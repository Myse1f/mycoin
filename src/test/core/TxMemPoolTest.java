/**
 * Created By Yufan Wu
 * 2019/2/26
 */
package core;

import org.junit.Test;

import static org.junit.Assert.*;

public class TxMemPoolTest {
    TxMemPool mempool = TxMemPool.getInstance();

    @Test
    public void addRemoveTest() {
        Transaction tx = new Transaction();
        mempool.add(tx.getHash(), tx);
        assertArrayEquals(mempool.getAllHash().toArray(), new SHA256Hash[]{tx.getHash()});
        assertTrue(mempool.remove(tx));
        assertArrayEquals(mempool.getAllHash().toArray(), new SHA256Hash[0]);
        assertFalse(mempool.remove(tx));
    }


}
