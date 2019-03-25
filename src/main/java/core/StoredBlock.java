/**
 * Created By Yufan Wu
 * 2019/3/25
 */
package core;

import exception.VerificationException;

import java.io.Serializable;
import java.math.BigInteger;

/**
 * Wrap a {@link Block} object with extra data that can be derived from the block chain in order to
 * save time re-compute them.
 * StoredBlocks are put inside BlockPersistence which saves them into memory or disk
 */
public class StoredBlock implements Serializable {
    private static final long serialVersionUID = -2693437206206842414L;

    private Block block;
    private BigInteger chainWork; // cumulative work from genesis block
    private int height; // height of the block in block chain from 0

    public StoredBlock(Block block, BigInteger chainWork, int height) {
        this.block = block;
        this.chainWork = chainWork;
        this.height = height;
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
        return this.block.equals(o.getBlock()) && this.chainWork.equals(o.getChainWork()) && this.height == o.getHeight();
    }

    @Override
    public int hashCode() {
        return block.hashCode() ^ chainWork.hashCode() ^ height;
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
    public StoredBlock getPreviousBlock(BlockPresistence source) {
        return source.get(block.getHashPrevBlock());
    }
}
