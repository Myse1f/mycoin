/**
 * Created By Yufan Wu
 * 2018/12/26
 */
package core;

import org.bouncycastle.util.encoders.Hex;

import java.io.Serializable;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

/**
 * Warp the byte[] to represent uint256 for sha-256 hash
 */
public class SHA256Hash implements Serializable {
    private static final long serialVersionUID = -3081155937186535182L;
    private static final int HASHCODE_BYTES_TO_CHECK = 5;

    public static final SHA256Hash ZERO_HASH = new SHA256Hash(new byte[32]);

    private byte[] bytes;
    private int hash = -1;

    /**
     * Construct a {@link SHA256Hash} with 32 length bytes array
     * @param bytes
     */
    public SHA256Hash(byte[] bytes) {
        assert bytes.length == 32;
        this.bytes = bytes;
    }

    private SHA256Hash(byte[] bytes, int hash) {
        assert bytes.length == 32;
        this.bytes = bytes;
        this.hash = hash;
    }

    /**
     * Construct a {@link SHA256Hash} with a hex string (length 64)
     * @param hash
     */
    public SHA256Hash(String hash) {
        assert hash.length() == 64;
        this.bytes = Hex.decode(hash);
    }

    /**
     * Calculates the (one-time) hash of contents and returns it as a new wrapped hash.
     */
    public static SHA256Hash create(byte[] contents) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            return new SHA256Hash(digest.digest(contents));
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);  // Cannot happen.
        }
    }

    /**
     * Returns true if the hashes are equal.
     */
    @Override
    public boolean equals(Object other) {
        if (!(other instanceof SHA256Hash)) {
            return false;
        }
        return Arrays.equals(bytes, ((SHA256Hash) other).bytes);
    }

    @Override
    public String toString() {
        return Utils.bytesToHexString(bytes);
    }

    @Override
    public int hashCode() {
        if (hash == -1) {
            hash = 1;
            for (int i = 0; i < HASHCODE_BYTES_TO_CHECK; i++)
                hash = 31 * hash + bytes[i];
        }
        return hash;
    }

    /**
     * Returns the bytes interpreted as a positive integer.
     */
    public BigInteger toBigInteger() {
        return new BigInteger(1, bytes);
    }

    public byte[] getBytes() {
        return bytes;
    }

    public SHA256Hash duplicate() {
        return new SHA256Hash(bytes, hash);
    }
}
