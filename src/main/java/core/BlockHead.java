/**
 * Created By Yufan Wu
 * 2019/1/7
 */
package core;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.Serializable;

/**
 * The header of a block
 */
public class BlockHead implements Serializable {
    private static final long serialVersionUID = 9212934684154939401L;
    private static final Logger logger = LoggerFactory.getLogger(BlockHead.class);

    protected SHA256Hash hashPrevBlock; // the hash of previous block
//    protected SHA256Hash hashMerkleRoot; // the merkle tree root
    /** java don't have unsigned int, thus use long */
    protected long nTime; // the time when the block generated
    protected long nBits; // the difficult target
    protected long nNonce; // the random number to proof of work

    /**
     * hashPrevBlock -- 32 bytes
     * nTime -- 4 bytes
     * nBits -- 4 bytes
     * nNonce -- 4 bytes
     */
    public static final int BLOCK_HEAD_SIZE = 44;

    public BlockHead() {
        setNull();
    }

    public BlockHead(SHA256Hash hashPrevBlock, long nTime, long nBits, long nNonce) {
        this.hashPrevBlock = hashPrevBlock;
        this.nTime = nTime;
        this.nBits = nBits;
        this.nNonce = nNonce;
    }

    public BlockHead(BlockHead other) {
        this.hashPrevBlock = other.hashPrevBlock.duplicate();
//        this.hashMerkleRoot = other.hashMerkleRoot.duplicate();
        this.nTime = other.nTime;
        this.nNonce = other.nNonce;
        this.nBits = other.nBits;
    }

    public void setNull() {
        hashPrevBlock = SHA256Hash.ZERO_HASH;
//        hashMerkleRoot = SHA256Hash.ZERO_HASH;
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

//    public SHA256Hash getHashMerkleRoot() {
//        return hashMerkleRoot;
//    }

//    public void setHashMerkleRoot(SHA256Hash hashMerkleRoot) {
//        this.hashMerkleRoot = hashMerkleRoot;
//    }

    /**
     * serialize to byte[] for disk storage
     * @return serialized data
     */
    public byte[] serialize() throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        bos.write(hashPrevBlock.getBytes());
        bos.write(Utils.uint32ToByteArrayLE(nTime));
        bos.write(Utils.uint32ToByteArrayLE(nBits));
        bos.write(Utils.uint32ToByteArrayLE(nNonce));
        return bos.toByteArray();
    }

    /**
     * deserialize data to a block head
     * @param data
     */
    public void deserialize(byte[] data) {
        byte[] hash = new byte[32];
        System.arraycopy(data, 0, hash, 0, 32);
        hashPrevBlock = new SHA256Hash(hash);
        nTime = Utils.readUint32LE(data, 32);
        nBits = Utils.readUint32LE(data, 32 + 4);
        nNonce = Utils.readUint32LE(data, 32 + 8);
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;

        if (o == null || getClass() != o.getClass()) return false;

        BlockHead blockHead = (BlockHead) o;

        return new EqualsBuilder()
                .append(nTime, blockHead.nTime)
                .append(nBits, blockHead.nBits)
                .append(nNonce, blockHead.nNonce)
                .append(hashPrevBlock, blockHead.hashPrevBlock)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
                .append(hashPrevBlock)
                .append(nTime)
                .append(nBits)
                .append(nNonce)
                .toHashCode();
    }
}
