/**
 * Created By Yufan Wu
 * 2019/1/7
 */
package core;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;

/**
 * The header of a block
 */
public class BlockHeader implements Serializable {
    private static final long serialVersionUID = 9212934684154939401L;
    private static final Logger logger = LoggerFactory.getLogger(BlockHeader.class);

    protected SHA256Hash hashPrevBlock; // the hash of previous block
    protected SHA256Hash hashMerkleRoot; // the merkle tree root
    /** java don't have unsigned int, thus use long */
    protected long nTime; // the time when the block generated
    protected long nBits; // the difficult target
    protected long nNonce; // the random number to proof of work

    public BlockHeader() {
        setNull();
    }

    public void setNull() {
        hashPrevBlock = SHA256Hash.ZERO_HASH;
        hashMerkleRoot = SHA256Hash.ZERO_HASH;
        nTime = 0;
        nNonce = 0;
        nBits = 0;
    }

    public boolean isNull() {
        return nBits == 0;
    }

    public SHA256Hash getHashPrevBlock() {
        return hashPrevBlock;
    }

    public void setHashPrevBlock(SHA256Hash hashPrevBlock) {
        this.hashPrevBlock = hashPrevBlock;
    }

    public SHA256Hash getHashMerkleRoot() {
        return hashMerkleRoot;
    }

    public void setHashMerkleRoot(SHA256Hash hashMerkleRoot) {
        this.hashMerkleRoot = hashMerkleRoot;
    }

    public long getnTime() {
        return nTime;
    }

    public void setnTime(long nTime) {
        this.nTime = nTime;
    }

    public long getnBits() {
        return nBits;
    }

    public void setnBits(long nBits) {
        this.nBits = nBits;
    }

    public long getnNonce() {
        return nNonce;
    }

    public void setnNonce(long nNonce) {
        this.nNonce = nNonce;
    }
}
