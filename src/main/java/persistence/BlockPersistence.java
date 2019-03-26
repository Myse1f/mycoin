/**
 * Created By Yufan Wu
 * 2019/3/26
 */
package persistence;

import core.SHA256Hash;
import core.StoredBlock;
import exception.BlockPersistenceException;

/**
 * Interface for block persistence, store to Disk or memory
 */
public interface BlockPersistence {
    /** store a block into database */
    void put(StoredBlock block) throws BlockPersistenceException;

    /** fetch a specified block from database */
    StoredBlock get(SHA256Hash hash) throws BlockPersistenceException;

    /** get the head block of the block chain */
    StoredBlock getChainTip() throws BlockPersistenceException;

    /** set the head of chain */
    void setChainTip(StoredBlock block) throws BlockPersistenceException;
}
