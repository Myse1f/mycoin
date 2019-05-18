/**
 * Created By Yufan Wu
 * 2019/5/14
 */
package core;

import main.Main;
import net.NetworkParameters;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import persistence.LevelDBBlockPersistence;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.CountDownLatch;


@RunWith(SpringRunner.class)
@SpringBootTest(classes = {Main.class})
public class MinerTest {

    private Miner miner;

    private CountDownLatch lock;

    @Before
    public void setup() throws Exception {
        File f = File.createTempFile("leveldb", null);
        f.delete();

        NetworkParameters params = new NetworkParameters(NetworkParameters.ID_TESTNET);
        LevelDBBlockPersistence persistence = new LevelDBBlockPersistence(params, f);
        persistence.reset();
        BlockChain chain = new BlockChain(persistence, params);
        lock = new CountDownLatch(1);
        PeerGroup network = new PeerGroup(chain, params) {
            // disable broadcasting, and stop when a block is mined
            @Override
            public void broadcastBlockInv(Inv blockInv) throws IOException {
                lock.countDown();
            }
        };
        miner = new Miner(chain, network);
    }

    @Test
    public void test() throws Exception {
        miner.run();
        lock.await();
        miner.stop();
    }
}
