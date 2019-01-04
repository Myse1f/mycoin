/**
 * Created By Yufan Wu
 * 2019/1/4
 */
package core;

/**
 * Store transactions in-memory
 * Transaction must be verified first and then add into pool
 * Pool is singleton globally
 */
public class TxMemPool {
    private static final TxMemPool mempool = new TxMemPool();

    //TODO
    private TxMemPool() {}

    public static TxMemPool getInstance() {
        return mempool;
    }
}
