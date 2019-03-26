/**
 * Created By Yufan Wu
 * 2019/3/26
 */
package persistence;

import core.Block;
import core.BlockHead;
import core.SHA256Hash;
import core.StoredBlock;
import org.junit.Test;

import java.io.IOException;
import java.math.BigInteger;
import java.nio.ByteBuffer;

public class DiskBlockPersistenceTest {
    public static final byte[] EMPTY_BYTES = new byte[16];

    @Test
    public void Test() throws IOException {
        StoredBlock block = new StoredBlock(new Block(new BlockHead(SHA256Hash.ZERO_HASH, 1, 2, 3)), new BigInteger("32"), 1);
        ByteBuffer buf = ByteBuffer.allocate(64);
        buf.putInt(block.getHeight());
        byte[] chainWorkBytes = block.getChainWork().toByteArray();
        /** chainWork use a constant 16 bytes to store, thus padding with 0 if necessary */
        if (chainWorkBytes.length < 16) {
            buf.put(EMPTY_BYTES, 0, 16 - chainWorkBytes.length);
        }
        buf.put(chainWorkBytes);
        buf.put(block.getBlock().serialize());
        System.out.println(buf.array());
    }
}
