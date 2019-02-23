/**
 * Created By Yufan Wu
 * 2019/1/4
 */
package core;

import java.util.HashMap;
import java.util.Map;

/**
 * Store transactions in-memory
 * Transaction must be verified first and then add into pool
 * Pool is singleton globally
 */
public class TxMemPool {
    private static final TxMemPool mempool = new TxMemPool();

    private long nTransactionsaUpdated; // increment when mempool update, check for its updated
    private Map<SHA256Hash, Transaction> mapTx;
    private Map<TransactionOutpoint, TransactionInput> mapNextTx;

    //TODO
    private TxMemPool() {
        nTransactionsaUpdated = 0;
        mapTx = new HashMap<>();
        mapNextTx = new HashMap<>();
    }

    public static TxMemPool getInstance() {
        return mempool;
    }
}
