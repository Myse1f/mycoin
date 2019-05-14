/**
 * Created By Yufan Wu
 * 2019/5/14
 */
package persistence;

import core.Block;
import core.StoredBlock;
import net.NetworkParameters;
import org.junit.Test;

import java.io.File;

import static org.junit.Assert.assertEquals;

public class LevelDBPersistenceTest {
    @Test
    public void test() throws Exception {
        File f = File.createTempFile("leveldb", null);
        f.delete();

        NetworkParameters params = new NetworkParameters(NetworkParameters.ID_TESTNET);
        LevelDBBlockPersistence persistence = new LevelDBBlockPersistence(params, f);
        persistence.reset();

        // Check the first block in a new store is the genesis block.
        StoredBlock genesis = persistence.getChainTip();
        assertEquals(params.genesisBlock, genesis.getBlock());
        assertEquals(0, genesis.getHeight());

        // Build a new block.
        Block block = new Block();
        block.setnBits(params.genesisBlock.getnBits());
        block.setHashPrevBlock(params.genesisBlock.getHash());
        StoredBlock b1 = genesis.build(block);
        persistence.put(b1);
        persistence.setChainTip(b1);
        persistence.close();

        // Check we can get it back out again if we rebuild the store object.
        persistence = new LevelDBBlockPersistence(params, f);
        try {
            StoredBlock b2 = persistence.get(b1.getBlock().getHash());
            assertEquals(b1, b2);
            // Check the chain head was stored correctly also.
            StoredBlock chainHead = persistence.getChainTip();
            assertEquals(b1, chainHead);
        } finally {
            persistence.close();
            persistence.destroy();
        }
    }
}
