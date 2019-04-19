/**
 * Created By Yufan Wu
 * 2019/4/16
 */
package core;

import exception.BlockPersistenceException;
import exception.ProtocolException;
import exception.VerificationException;

import java.io.IOException;
import java.math.BigInteger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * A miner is trying to solve the hash puzzle It travel all the possible nonce
 * until the PoW is reached or a new block tip is concatenated
 */
@Component("Miner")
public class Miner {
    private static Logger logger = LoggerFactory.getLogger(Miner.class);

    private BlockChain blockChain;
    private PeerGroup peerGroup;
    private boolean working;

    private MinerThread miner;

    @Autowired
    public Miner(BlockChain blockChain, PeerGroup peerGroup) {
        this.blockChain = blockChain;
        this.peerGroup = peerGroup;
        this.working = false;
    }

    /**
     * start solving puzzle, launch mining thread
     */
    public synchronized void run() {
        this.working = true;
        this.miner = new MinerThread();
        miner.start();
        logger.info("Start mining block");
    }

    /**
     * stop mining, interrupt the thread
     */
    public synchronized void stop() {
        this.working = false;
        this.miner = null; // finalize the miner
        logger.info("Stop mining block.");
    }

    public synchronized boolean isWorking() {
        return this.working;
    }

    /**
     * When a block is mined 1. add to the block chain 2. broadcast the inv to all
     * peers
     */
    private void handleBlockMined(Block block) {
        logger.info("New block is mined: {}", block);
        try {
            blockChain.add(block);
            peerGroup.brocastBlcokInv(new Inv(Inv.InvType.MSG_BLOCK, block.getHash()));
        } catch (VerificationException e) {
            // impossible, thus the block is mined after verification
            throw new RuntimeException(e);
        } catch (BlockPersistenceException e) {
            // database has some problem
            logger.error("Error when adding block into chain.");
            throw new RuntimeException(e);
        } catch (IOException e) {
            logger.error("Error while broadcast new mined block inv to peers.");
        }
    }

    /**
     * create a block template for mining with nTime and nNonce unset
     */
    private Block createBlockTemplate(StoredBlock prevBlock) throws ProtocolException, BlockPersistenceException {
        return new Block(
                new BlockHead(prevBlock.getBlock().getHash(), -1, StoredBlock.getNextnBits(prevBlock, blockChain.getBlockPersistence()), 0)
        );
    }

    private class MinerThread extends Thread {

        public MinerThread() {
            super("Miner Thread");
            setPriority(Math.max(Thread.MIN_PRIORITY, Thread.currentThread().getPriority()-1));
            setDaemon(true);
        }

        /**
         * while running:
         * 1. get the chainTip and construct next block template
         * 2. set the nTime and nNonce
         * 3. increment nNonce until 
         *      a. nNonce is found -- add the new block to blockchain and broadcast the Inv
         *      b. nNonce is overflow -- update the time field, go to step 1 and continue
         *      c. chainTip is updated -- a new chainTip is received, go to step 1 and continue
         */
        @Override
        public void run() {
            try {
                while (isWorking()) {
                    StoredBlock prevChainTip = blockChain.getChainTip();
                    Block template = createBlockTemplate(prevChainTip);
                    template.setnTime(System.currentTimeMillis()/1000); // TODO in decenteralized system, the time need to be set smarter
                    template.setnNonce(0);
                    while (isWorking()) {
                        SHA256Hash hash = new SHA256Hash(Utils.reverseBytes(Utils.doubleDigest(Utils.objectsToByteArray((BlockHead)template))));
                        BigInteger target = template.getnBitsAsInteger();
                        BigInteger current = hash.toBigInteger();
                        if (current.compareTo(target) <= 0) {
                            // available nNonce is found
                            handleBlockMined(template);
                            break;
                        }

                        long nonce = template.getnNonce();
                        if (nonce + 1 > 0xFFFFFFFFL) {
                            // nNonce is overflow
                            break;
                        }
                        if (prevChainTip.equals(blockChain.getChainTip())) {
                            // chainTip is updated
                            break;
                        }
                        template.setnNonce(nonce + 1);
                    }
                }
            } catch (Exception e) {
                logger.error("Error in when mining, start shutdown miner.");
                Miner.this.stop();
            }
        }
    }
}
