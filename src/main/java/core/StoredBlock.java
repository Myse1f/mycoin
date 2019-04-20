/**
 * Created By Yufan Wu
 * 2019/3/25
 */
package core;

import exception.BlockPersistenceException;
import exception.ProtocolException;
import exception.VerificationException;
import net.NetworkParameters;
import persistence.BlockPersistence;
import utils.SpringContextUtil;

import java.io.IOException;
import java.io.Serializable;
import java.math.BigInteger;
import java.nio.ByteBuffer;

/**
 * Wrap a {@link Block} object with extra data that can be derived from the block chain in order to
 * save time re-compute them.
 * StoredBlocks are put inside BlockPersistence which saves them into memory or disk
 */
public class StoredBlock implements Serializable {
    private static final long serialVersionUID = -2693437206206842414L;
    private static final int CHAIN_WORK_BYTES = 16; // 16 bytes for chainwork storage
    public static final int SIZE = 4 + CHAIN_WORK_BYTES + Block.BLOCK_HEAD_SIZE + SHA256Hash.SIZE; // 4 bytes for height
    private static final byte[] EMPTY_BYTES = new byte[CHAIN_WORK_BYTES];

    private Block block;
    private BigInteger chainWork; // cumulative work from genesis block
    private int height; // height of the block in block chain from 0
    private SHA256Hash next; // next block's hash, the field of chainTip is 0

    public StoredBlock(Block block, BigInteger chainWork, int height, SHA256Hash next) {
        this.block = block;
        this.chainWork = chainWork;
        this.height = height;
        this.next = next;
    }

    public StoredBlock(Block block, BigInteger chainWork, int height) {
        this.block = block;
        this.chainWork = chainWork;
        this.height = height;
        this.next = SHA256Hash.ZERO_HASH;
    }

    public Block getBlock() {
        return block;
    }

    public BigInteger getChainWork() {
        return chainWork;
    }

    public int getHeight() {
        return height;
    }

    public SHA256Hash getNext() {
        return this.next;
    }

    public void setNext(SHA256Hash nextHash) {
        this.next = nextHash;
    }

    /** compare whether the block has more work than the other */
    public boolean moreWorkThan(StoredBlock other) {
        return chainWork.compareTo(other.chainWork) > 0;
    }

    @Override
    public boolean equals(Object other) {
        if (!(other instanceof StoredBlock)) {
            return false;
        }
        StoredBlock o = (StoredBlock)other;
        return this.block.equals(o.getBlock()) && this.chainWork.equals(o.getChainWork()) && this.height == o.getHeight() && this.next.equals(o.getNext());
    }

    @Override
    public int hashCode() {
        return block.hashCode() ^ chainWork.hashCode() ^ height ^ next.hashCode();
    }

    @Override
    public String toString() {
        return String.format("Block %s at height %d: %s", block.getHash().toString(), height, block.toString());
    }

    /**
     * Creates a new StoredBlock based on this block
     * @param block
     * @return
     * @throws VerificationException
     */
    public StoredBlock build(Block block) throws VerificationException {
        BigInteger chainWork = this.chainWork.add(block.getWork());
        int height = this.height + 1;
        return new StoredBlock(block, chainWork, height);
    }

    /**
     * Given a block persistence source, get the previous storedblock from it
     * @param source block persistence source
     * @return previous block's hash or null if not found
     */
    public StoredBlock getPreviousBlock(BlockPersistence source) throws BlockPersistenceException {
        return source.get(block.getHashPrevBlock());
    }

    public StoredBlock getNextBlock(BlockPersistence source) throws BlockPersistenceException {
        return source.get(next);
    }

    /**
     * serialize the block into ByteBuffer
     */
    public void serialize(ByteBuffer buf) throws IOException {
        buf.putInt(height);
        byte[] chainWorkBytes = chainWork.toByteArray();
        /** chainWork use a constant 16 bytes to store, thus padding with 0 if necessary */
        if (chainWorkBytes.length < CHAIN_WORK_BYTES) {
            buf.put(EMPTY_BYTES, 0, CHAIN_WORK_BYTES - chainWorkBytes.length);
        }
        buf.put(chainWorkBytes);
        buf.put(next.getBytes());
        buf.put(block.serialize());
        buf.position(0);
    }

    /**
     * deserialize the buf into a StoredBlock
     */
    public static StoredBlock deserialize(ByteBuffer buf) {
        byte[] chainWorkBytes = new byte[StoredBlock.CHAIN_WORK_BYTES];
        int height = buf.getInt();
        buf.get(chainWorkBytes);
        BigInteger chainWork = new BigInteger(1, chainWorkBytes);
        byte[] next = new byte[SHA256Hash.SIZE];
        buf.get(next);
        SHA256Hash hashNext = new SHA256Hash(next);
        byte[] header = new byte[BlockHead.BLOCK_HEAD_SIZE];
        buf.get(header);
        BlockHead block = Block.deserialize(header);
        return new StoredBlock(new Block(block), chainWork, height, hashNext);
    }

    /**
     * base the block, get the nBits of next block according difficulty adaption
     */
    public static long getNextnBits(StoredBlock prevBlock, BlockPersistence source) throws BlockPersistenceException, ProtocolException {
        Block prev = prevBlock.getBlock();
        int blocksInterval = ((NetworkParameters)(SpringContextUtil.getBean("network_params"))).interval;
        // check interval
        if ((prevBlock.getHeight() + 1) % blocksInterval != 0) {
            return prev.getnBits();
        }

        StoredBlock cursor = source.get(prev.getHash());
        for (int i = 0; i < blocksInterval - 1; i++) {
            if (cursor == null) {
                throw new ProtocolException("Difficulty transition point but we dit not find a way back to genesis.");
            }
            cursor = source.get(cursor.getBlock().getHashPrevBlock());
        }
        Block intervalStart = cursor.getBlock();
        int timespan = (int)(prev.getnTime() - intervalStart.getnTime());
        int targetTimespan = ((NetworkParameters)(SpringContextUtil.getBean("network_params"))).targetTimespan;
        // Limit the adjustment step.
        if (timespan < targetTimespan / 4)
            timespan = targetTimespan / 4;
        if (timespan > targetTimespan * 4)
            timespan = targetTimespan * 4;

        BigInteger newnBits = Utils.decodeCompactBits(intervalStart.getnBits());
        newnBits = newnBits.multiply(BigInteger.valueOf(timespan));
        newnBits = newnBits.divide(BigInteger.valueOf(targetTimespan));

        if (newnBits.compareTo(((NetworkParameters)(SpringContextUtil.getBean("network_params"))).proofOfWorkLimit) > 0) {
            newnBits = ((NetworkParameters)(SpringContextUtil.getBean("network_params"))).proofOfWorkLimit;
        }

        return Utils.encodeCompactBits(newnBits);
    }
}
