/**
 * Created By Yufan Wu
 * 2018/12/25
 */
package core;

import exception.BlockPersistenceException;
import exception.VerificationException;
import net.NetworkParameters;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import persistence.BlockPersistence;
import utils.SpringContextUtil;

import java.math.BigInteger;
import java.util.*;

/**
 * A BlockChain holds a series of {@link Block} objects, links them together, and knows how to verify that the
 * chain follows the rules of the {@link net.NetworkParameters} for this chain.<p>
 */
@Component("BlockChain")
public class BlockChain {
    private static final Logger logger = LoggerFactory.getLogger(BlockChain.class);

    /** the persistence database of block file */
    protected final BlockPersistence blockPersistence;

    private NetworkParameters params;

    /** The tip block of the chain */
    protected StoredBlock chainTip;

    /**
     * cahce for main chain block
     */
//    private Set<SHA256Hash> mainChainBlocks;

    /**
     * chainHead is accessed under this lock rather than the BlockChain lock. This is to try and keep accessors
     * responsive whilst the chain is downloading
     */
    protected final Object chainTipLock = new Object();

    /** cache the unconnected blocks until they can be insert into chain */
    private final List<Block> unconnectedBlocks = new ArrayList<>();

    /** initialize with a block persistence database */
    @Autowired
    public BlockChain(BlockPersistence blockPersistence, NetworkParameters params) throws BlockPersistenceException {
        this.blockPersistence = blockPersistence;
        this.params = params;
        this.chainTip = blockPersistence.getChainTip();

//        this.mainChainBlocks = new HashSet<>();
//        // initialize cache, it may need a little time
//        logger.info("Initializing index of main chain block... It may need a some time");
//        for (StoredBlock cursor = chainTip; cursor != null; cursor = cursor.getPreviousBlock(this.blockPersistence)) {
//            mainChainBlocks.add(cursor.getBlock().getHash());
//        }
//        logger.info("Initialize Finished!");

        logger.info("chain tip is at height {}:\n{}", chainTip.getHeight(), chainTip.getBlock());
    }

    public BlockPersistence getBlockPersistence() {
        return blockPersistence;
    }

    // for logger info
    private long lastAddTime = System.currentTimeMillis();
    private long blocksAdded;


    public synchronized boolean add(Block block) throws VerificationException, BlockPersistenceException {
        return add(block, true);
    }

    /**
     * add block to the chain
     * @param block target block
     * @param connect if true, try to connect unconnected block to the tip
     * @return true if add successfully, otherwise false
     */
    public synchronized boolean add(Block block, boolean connect) throws BlockPersistenceException, VerificationException {
        if (System.currentTimeMillis() - lastAddTime > 1000) {
            if (blocksAdded > 1) {
                logger.debug("{} blocks per second.", blocksAdded);
            }
            lastAddTime = System.currentTimeMillis();
            blocksAdded = 0;
        }

        /** check the chain tip */
        if (block.equals(chainTip.getBlock())) {
            logger.debug("Chain tip {} has already been added!", block.getHash());
            return true;
        }

        if(!block.verifyBlock()) {
            logger.error("Fail to verify block: ", block.getHash().toString());
            throw new VerificationException("Fail to verify block.");
        }

        StoredBlock prevBlock = blockPersistence.get(block.getHashPrevBlock());

        if (prevBlock == null) {
            /** can't find previous block in database, thus put it in unconnected blocks list */
            logger.info("Block {} can't be connected.", block.getHash());
            unconnectedBlocks.add(block);
            return false;
        } else {
            /** connect to the chain */
            StoredBlock newBlock = prevBlock.build(block);
            checkDifficultAdaption(prevBlock, newBlock); // check whether difficulty is correct
            blockPersistence.put(newBlock);
            connectBlock(newBlock, prevBlock);
        }

        /** after add a new block, try to connect unconnected blocks */
        if (connect) {
            tryConnectUnconnectedBlocks();
        }

        blocksAdded++;
        return true;
    }

    /**
     * check the if difficulty adaption is correct
     * every Networkparameters.interval change once
     * @param prevBlock
     * @param newBlock
     * @throws VerificationException
     * @throws BlockPersistenceException
     */
    private void checkDifficultAdaption(StoredBlock prevBlock, StoredBlock newBlock) throws VerificationException, BlockPersistenceException {
        Block prev = prevBlock.getBlock();
        Block current = newBlock.getBlock();
        int blocksInterval = ((NetworkParameters)(SpringContextUtil.getBean("network_params"))).interval;
        // check interval
        if ((prevBlock.getHeight() + 1) % blocksInterval != 0) {
            // don't change difficulty, check consistency
            if (current.getnBits() != prev.getnBits()) {
                throw new VerificationException("Unexpected change in difficulty at height " + prevBlock.getHeight() +
                        ": " + Long.toHexString(current.getnBits()) + " vs " +
                        Long.toHexString(prev.getnBits()));
            }
            return;
        }

        StoredBlock cursor = blockPersistence.get(prev.getHash());
        for (int i = 0; i < blocksInterval - 1; i++) {
            if (cursor == null) {
                throw new VerificationException("Difficulty transition point but we dit not find a way back to genesis.");
            }
            cursor = blockPersistence.get(cursor.getBlock().getHashPrevBlock());
        }

        Block intervalStart = cursor.getBlock();
        int timespan = (int)(prev.getnTime() - intervalStart.getnTime());
        int targetTimespan = params.targetTimespan;
        // Limit the adjustment step.
        if (timespan < targetTimespan / 4)
            timespan = targetTimespan / 4;
        if (timespan > targetTimespan * 4)
            timespan = targetTimespan * 4;

        BigInteger newnBits = Utils.decodeCompactBits(intervalStart.getnBits());
        newnBits = newnBits.multiply(BigInteger.valueOf(timespan));
        newnBits = newnBits.divide(BigInteger.valueOf(targetTimespan));

        if (newnBits.compareTo(params.proofOfWorkLimit) > 0) {
            logger.debug("Difficulty hit proof of work limit: {}", newnBits.toString(16));
            newnBits = params.proofOfWorkLimit;
        }

        int accuracyBytes = (int)(current.getnBits() >>> 24) - 3;
        BigInteger receivednBits = current.getnBitsAsInteger();
        BigInteger mask = BigInteger.valueOf(0xFFFFFFL).shiftLeft(accuracyBytes * 8);
        newnBits = newnBits.and(mask);
        if(newnBits.compareTo(receivednBits) != 0) {
            throw new VerificationException("Network provided difficulty bits do not match what was calculated: " +
                    receivednBits.toString(16) + " vs " + newnBits.toString(16));
        }
    }

    /**
     * connect the new block into the block chain, set the tip of chain
     * @param newBlock
     * @param prevBlock
     */
    private void connectBlock(StoredBlock newBlock, StoredBlock prevBlock) throws BlockPersistenceException {
        if (prevBlock.equals(chainTip)) {
            // a new block, first set the prevblock's next field
            chainTip.setNext(newBlock.getBlock().getHash());
            blockPersistence.put(chainTip);
            // set new chain tip
            setChainTip(newBlock);
//            mainChainBlocks.add(newBlock.getBlock().getHash());
            logger.debug("Chain height is now {}.", chainTip.getHeight());
        } else {
            // not a tip
            // test the work, if re-organize is necessary
            boolean reorg = newBlock.moreWorkThan(chainTip);
            if (reorg) {
                logger.info("Block is causing re-organize.");
                handleReOrganize(newBlock);
            } else {
                StoredBlock splitPoint = findSplit(newBlock, chainTip);
                if (splitPoint.equals(newBlock)) {
                    // the block is duplicated
                    logger.debug("Duplicated block received at height {}.", newBlock.getHeight());
                } else {
                    int splitPointHeight = splitPoint != null ? splitPoint.getHeight() : -1;
                    String splitPointHash = splitPoint != null ? splitPoint.getBlock().getHash().toString() : "?";
                    logger.info("Block forks the chain at height {}/block {}, but it did not cause a reorganize:\n{}", splitPointHeight, splitPointHash, newBlock);
                }
            }
        }
    }

    /**
     * find the split point of two block
     * e.g.   A <- B <- C <- D
     *               <- E <- F
     * the split point of D and F is B
     * @param newBlock
     * @param chainTip
     * @return the split point block
     * @throws BlockPersistenceException
     */
    private StoredBlock findSplit(StoredBlock newBlock, StoredBlock chainTip) throws BlockPersistenceException {
        StoredBlock currentTip = chainTip;
        StoredBlock newTip = newBlock;
        // trace back until we find the common block
        while (!currentTip.equals(newTip)) {
            if (currentTip.getHeight() > newTip.getHeight()) {
                currentTip = currentTip.getPreviousBlock(blockPersistence);
            } else {
                newTip = newTip.getPreviousBlock(blockPersistence);
            }
        }
        return currentTip;
    }


    /**
     * try to connect cached unconnected blocks
     */
    private void tryConnectUnconnectedBlocks() throws BlockPersistenceException, VerificationException {
        int blockConnected = 0;
        do {
            blockConnected = 0;
            Iterator<Block> it = unconnectedBlocks.iterator();
            while (it.hasNext()) {
                Block block = it.next();
                logger.debug("Try to connect {}", block.getHash());
                StoredBlock prev = blockPersistence.get(block.getHashPrevBlock());
                if (prev == null) {
                    logger.debug("  Can't be connected.");
                    continue;
                }
                add(block, false); // set false to avoid recurse infinitely
                it.remove();
                blockConnected++;
            }
            if (blockConnected > 0) {
                logger.debug("Connected {} unconnected blocks.", blockConnected);
            }
        } while (blockConnected > 0);
    }

    /**
     * re-organize the block, set and unset the next field
     */
    private void handleReOrganize(StoredBlock newBlock) throws BlockPersistenceException {
        StoredBlock splitPoint = findSplit(newBlock, chainTip);

        List<StoredBlock> oldBlocks = getPartialChain(chainTip, splitPoint);
        List<StoredBlock> newBlocks = getPartialChain(newBlock, splitPoint);
        for (StoredBlock block : oldBlocks) {
            block.setNext(SHA256Hash.ZERO_HASH);
            blockPersistence.put(block);
        }
        for (int i = 1; i < newBlocks.size(); i++) {
            StoredBlock block = newBlocks.get(i);
            block.setNext(newBlocks.get(i-1).getBlock().getHash());
            blockPersistence.put(block);
        }
        logger.info("Re-organize after split at height {}", splitPoint.getHeight());
        logger.info("Old chain head: {}", chainTip.getBlock().getHash().toString());
        logger.info("New chain head: {}", newBlock.getBlock().getHash().toString());
        logger.info("Split at block: {}", splitPoint.getBlock().getHash().toString());
        setChainTip(newBlock);
    }

    /**
     * Returns the set of contiguous blocks between 'higher' and 'lower'. lower is included, higher is not.
     */
    private List<StoredBlock> getPartialChain(StoredBlock higher, StoredBlock lower) throws BlockPersistenceException {
        assert higher.getHeight() > lower.getHeight();
        LinkedList<StoredBlock> results = new LinkedList<>();
        StoredBlock cursor = higher.getPreviousBlock(blockPersistence);
        while (true) {
            results.add(cursor);
            assert cursor != null : "Ran off the end of the chain";
            if (cursor.equals(lower)) {
                break;
            }
            cursor = cursor.getPreviousBlock(blockPersistence);
        }
        return results;
    }

    public StoredBlock getChainTip() {
        synchronized (chainTipLock) {
            return chainTip;
        }
    }

    public void setChainTip(StoredBlock chainTip) throws BlockPersistenceException {
        blockPersistence.setChainTip(chainTip);
        synchronized (chainTipLock) {
            this.chainTip = chainTip;
        }
    }

    public int getChainHeight() {
        return getChainTip().getHeight();
    }

    /**
     * if the block in the database
     */
    public boolean hasBlock(SHA256Hash hash) throws BlockPersistenceException {
        return blockPersistence.get(hash) != null;
    }

    /**
     * If the block in the main chain
     */
    public boolean isMainBlock(SHA256Hash hash) throws BlockPersistenceException {
        return !blockPersistence.get(hash).getNext().equals(SHA256Hash.ZERO_HASH);
    }
}
