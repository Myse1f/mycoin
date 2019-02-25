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
    private Map<TransactionOutpoint, TransactionInpoint> mapNextTx;

    private TxMemPool() {
        nTransactionsaUpdated = 0;
        mapTx = new HashMap<>();
        mapNextTx = new HashMap<>();
    }


    public static TxMemPool getInstance() {
        return mempool;
    }

    /**
     * add tx into the memory pool
     * @param hash the hash of tx
     * @param tx corresponding transaction
     * @return true if add into the pool
     */
    public synchronized boolean add(SHA256Hash hash, Transaction tx) {
        mapTx.put(hash, tx);
        for (int i = 0; i < tx.getInputs().size(); i++) {
            mapNextTx.put(tx.getInputs().get(i).getPrevout(), new TransactionInpoint(tx, i));
        }
        nTransactionsaUpdated++;
        return true;
    }

    /**
     * remove the tx from mempool
     * @param tx transaction to be removed
     * @return true if success
     */
    public synchronized boolean remove(Transaction tx) {
        SHA256Hash hash = tx.getHash();
        if (mapTx.containsKey(hash)) {
            for (TransactionInput input : tx.getInputs()) {
                mapNextTx.remove(input.getPrevout());
            }
            mapTx.remove(hash);
            nTransactionsaUpdated++;
            return true;
        } else {
            return false;
        }
    }

    /**
     * remove transaction conflicted with input tx
     * @param tx
     */
    public synchronized void removeConflicts(Transaction tx) {
        for (TransactionInput in : tx.getInputs()) {
            if (mapNextTx.containsKey(in.getPrevout())) {
                Transaction txConflict = mapNextTx.get(in.getPrevout()).getTx();
                if (!tx.equals(txConflict)) {
                    remove(txConflict);
                }
            }
        }
    }

    /**
     * clear the mempool
     */
    public synchronized void clear() {
        mapTx.clear();
        mapNextTx.clear();
        nTransactionsaUpdated++;
    }

    //TODO

    public synchronized void update() {
        nTransactionsaUpdated++;
    }

    public long getnTransactionsaUpdated() {
        return nTransactionsaUpdated;
    }
}
