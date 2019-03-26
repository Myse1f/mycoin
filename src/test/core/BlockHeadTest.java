/**
 * Created By Yufan Wu
 * 2019/3/26
 */
package core;


import net.NetworkParameters;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;

import static org.junit.Assert.*;

public class BlockHeadTest {
    @Before
    public void beforeTest() {
        NetworkParameters.setNetworkParameters(NetworkParameters.ID_TESTNET);
    }

    @Test
    public void blockHeadSizeTest() throws IOException {
        assertEquals(BlockHead.BLOCK_HEAD_SIZE, new BlockHead(SHA256Hash.ZERO_HASH, 1, 2, 3).serialize().length);
        assertEquals(BlockHead.BLOCK_HEAD_SIZE, new Block().serialize().length);
    }

    @Test
    public void blockHeadSerializeTest() throws IOException {
        BlockHead block = new BlockHead(SHA256Hash.ZERO_HASH, 1, 2, 3);
        byte[] serializedData = block.serialize();
        BlockHead other = new BlockHead();
        other.deserialize(serializedData);
        assertEquals(block, other);
    }
}
