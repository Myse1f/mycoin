/**
 * Created By Yufan Wu
 * 2018/12/23
 */
package core;

import com.alibaba.fastjson.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;

public class Utils {
    private static final Logger logger = LoggerFactory.getLogger(Utils.class);

    /**
     * calculate SHA-256 hash once
     * @param input buffer to be hashed
     * @param offset begin offset
     * @param length hash bytes length
     * @return hash result
     */
    public static byte[] singleDigest(byte[] input, int offset, int length) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            digest.update(input, offset, length);
            return digest.digest();
        } catch (NoSuchAlgorithmException e) {
            // impossible
            throw new RuntimeException(e);
        }
    }

    /**
     * calculate SHA-256 hash twice -- hash(hash(input))
     * @param input
     * @param offset
     * @param length
     * @return hash result
     */
    public static byte[] doubleDigest(byte[] input, int offset, int length) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            digest.update(input, offset, length);
            byte[] first = digest.digest();
            return digest.digest(first);
        } catch (NoSuchAlgorithmException e) {
            // impossible
            throw new RuntimeException(e);
        }
    }

    public static byte[] doubleDigest(byte[] input) {
        return doubleDigest(input, 0, input.length);
    }

    /**
     * Caculate SHA-256 with 2 buffer -- hash(hash(byte range 1 + byte range 2))
     * @param input1 buffer1
     * @param offset1
     * @param length1
     * @param input2 buffer2
     * @param offset2
     * @param length2
     * @return hash result
     */
    public static byte[] doubleDigestTwoBuffers(byte[] input1, int offset1, int length1,
                                               byte[] input2, int offset2, int length2) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            digest.update(input1, offset1, length1);
            digest.update(input2, offset2, length2);
            byte[] first = digest.digest();
            return digest.digest(first);
        } catch (NoSuchAlgorithmException e) {
            //impossible
            throw new RuntimeException(e);
        }
    }

    /**
     * Returns a copy of the given byte array in reverse order.
     */
    public static byte[] reverseBytes(byte[] bytes) {
        byte[] buf = new byte[bytes.length];
        for (int i = 0; i < bytes.length; i++)
            buf[i] = bytes[bytes.length - 1 - i];
        return buf;
    }

    /**
     * convert 4 bytes into int in big endian
     * @param b
     * @return int in big endian
     */
    public static int byteArrayToInt(byte[] b) {
        return   b[3] & 0xFF |
                (b[2] & 0xFF) << 8 |
                (b[1] & 0xFF) << 16 |
                (b[0] & 0xFF) << 24;
    }

    /**
     * Serialize given objects to byte array
     * @param objects
     * @return serialized bytes
     * @throws IOException
     */
    public static <T extends Serializable> byte[] objectsToByteArray(List<T> objects) throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(bos);
        byte[] ret;
        for (Object object : objects) {
            oos.writeObject(object);
        }
        oos.flush();
        ret = bos.toByteArray();
        oos.close();
        bos.close();

        return ret;
    }

    public static <T extends Serializable> byte[] objectsToByteArray(T object) throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(bos);
        byte[] ret;
        oos.writeObject(object);
        oos.flush();
        ret = bos.toByteArray();
        oos.close();
        bos.close();

        return ret;
    }

    /**
     * get the serialized size of a serializable object
     * @param obj
     * @return the serialized size
     */
    public static <T extends Serializable> int getObjectSerializedSize(T obj) {
        int size = 0;
        DumbOutputStream buf = new DumbOutputStream();
        try (ObjectOutputStream oos = new ObjectOutputStream(buf)) {
            oos.writeObject(obj);
            size = buf.count;
        } catch (IOException e) {
            logger.error(e.toString());
        }
        return size;
    }

    /**
     * for counting object size
     */
    private static class DumbOutputStream extends OutputStream {
        int count = 0;
        /**
         * the write method write 1 byte into buffer every time, which is the 8 lower bits of b
         * @param b
         * @throws IOException
         */
        @Override
        public void write(int b) throws IOException {
            count ++;
        }
    }

    /**
     * Returns the given byte array hex encoded.
     * @param bytes the given byte array
     * @return Hex String
     */
    public static String bytesToHexString(byte[] bytes) {
        StringBuilder buf = new StringBuilder(bytes.length * 2);
        for (byte b : bytes) {
            String s = Integer.toString(0xFF & b, 16);
            if (s.length() < 2)
                buf.append('0');
            buf.append(s);
        }
        return buf.toString();
    }

    public static long readUint32LE(byte[] bytes, int offset) {
        return ((bytes[offset++] & 0xFFL) << 0) |
                ((bytes[offset++] & 0xFFL) << 8) |
                ((bytes[offset++] & 0xFFL) << 16) |
                ((bytes[offset] & 0xFFL) << 24);
    }

    public static long readUint32BE(byte[] bytes, int offset) {
        return ((bytes[offset + 0] & 0xFFL) << 24) |
                ((bytes[offset + 1] & 0xFFL) << 16) |
                ((bytes[offset + 2] & 0xFFL) << 8) |
                ((bytes[offset + 3] & 0xFFL) << 0);
    }

    /**
     * MPI encoded numbers are produced by the OpenSSL BN_bn2mpi function. They consist of
     * a 4 byte big endian length field, followed by the stated number of bytes representing
     * the number in big endian format (with a sign bit).
     */
    public static BigInteger decodeMPI(byte[] mpi) {
        byte[] buf;
        int length = (int) readUint32BE(mpi, 0);
        buf = new byte[length];
        System.arraycopy(mpi, 4, buf, 0, length);

        if (buf.length == 0)
            return BigInteger.ZERO;
        boolean isNegative = (buf[0] & 0x80) == 0x80;
        if (isNegative)
            buf[0] &= 0x7f;
        BigInteger result = new BigInteger(buf);
        return isNegative ? result.negate() : result;
    }
    /**
     * The representation of nBits uses another home-brew encoding, as a way to represent a large
     * hash value in only 32 bits.
     */
    public static BigInteger decodeCompactBits(long compact) {
        int size = ((int) (compact >> 24)) & 0xFF;
        byte[] bytes = new byte[4 + size];
        bytes[3] = (byte) size;
        if (size >= 1) bytes[4] = (byte) ((compact >> 16) & 0xFF);
        if (size >= 2) bytes[5] = (byte) ((compact >> 8) & 0xFF);
        if (size >= 3) bytes[6] = (byte) ((compact >> 0) & 0xFF);
        return decodeMPI(bytes);
    }

    /**
     * convert long value into byte array in big-endian
     * @param val
     * @param out
     * @param offset
     */
    public static void uint32ToByteArrayBE(long val, byte[] out, int offset) {
        out[offset + 0] = (byte) (0xFF & (val >> 24));
        out[offset + 1] = (byte) (0xFF & (val >> 16));
        out[offset + 2] = (byte) (0xFF & (val >> 8));
        out[offset + 3] = (byte) (0xFF & (val >> 0));
    }

    /**
     * convert long value to byte array in little-endian
     * @param value
     * @return converted byte array
     */
    public static byte[] uint32ToByteArrayLE(long value) {
        byte[] out = new byte[4];
        out[0] = (byte)(0xFF & (value >> 0));
        out[1] = (byte)(0xFF & (value >> 8));
        out[2] = (byte)(0xFF & (value >> 16));
        out[3] = (byte)(0xFF & (value >> 24));
        return out;
    }

    /**
     * MPI encoded numbers are produced by the OpenSSL BN_bn2mpi function. They consist of
     * a 4 byte big endian length field, followed by the stated number of bytes representing
     * the number in big endian format (with a sign bit).
     */
    public static byte[] encodeMPI(BigInteger value) {
        if (value.equals(BigInteger.ZERO)) {
            return new byte[] {0x00, 0x00, 0x00, 0x00};
        }
        boolean isNegative = value.compareTo(BigInteger.ZERO) < 0;
        if (isNegative)
            value = value.negate();
        byte[] array = value.toByteArray();
        int length = array.length;
        if ((array[0] & 0x80) == 0x80)
            length++;

        byte[] result = new byte[length + 4];
        System.arraycopy(array, 0, result, length - array.length + 4, array.length);
        uint32ToByteArrayBE(length, result, 0);
        if (isNegative)
            result[4] |= 0x80;
        return result;

    }

    public static long encodeCompactBits(BigInteger value) {
        long result;
        int size = value.toByteArray().length;
        if (size <= 3)
            result = value.longValue() << 8 * (3 - size);
        else
            result = value.shiftRight(8 * (size - 3)).longValue();
        // The 0x00800000 bit denotes the sign.
        // Thus, if it is already set, divide the mantissa by 256 and increase the exponent.
        if ((result & 0x00800000L) != 0) {
            result >>= 8;
            size++;
        }
        result |= size << 24;
        result |= value.signum() == -1 ? 0x00800000 : 0;
        return result;
    }

    /**
     * convert a stored block to json object
     */
    public static JSONObject storedBlock2Json(StoredBlock block) {
        JSONObject json = new JSONObject();
        json.put("hash", block.getBlock().getHash().toString());
        json.put("height", block.getHeight());
        json.put("prevHash", block.getBlock().getHashPrevBlock().toString());
        json.put("time", block.getBlock().getnTime());
        json.put("nonce", block.getBlock().getnNonce());
        json.put("difficulty", block.getBlock().getnBits());

        return json;
    }

    /**
     * convert a peer to json object
     */
    public static JSONObject peer2Json(Peer peer) {
        JSONObject json = new JSONObject();
        json.put("isRunning", peer.isRunning());
        json.put("address", peer.getAddress().getAddr().getHostAddress());
        json.put("port", peer.getAddress().getPort());

        return json;
    }
}
