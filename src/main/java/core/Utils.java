/**
 * Created By Yufan Wu
 * 2018/12/23
 */
package core;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

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
    public static byte[] doubleDigestTwoBuffer(byte[] input1, int offset1, int length1,
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
    public static byte[] ObjectsToByteArray(Object ... objects) throws IOException {
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
}
