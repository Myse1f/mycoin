/**
 * Created By Yufan Wu
 * 2019/3/27
 */
package persistence;

import core.Block;
import core.SHA256Hash;
import core.StoredBlock;
import exception.BlockPersistenceException;
import exception.VerificationException;
import net.NetworkParameters;
import org.fusesource.leveldbjni.JniDBFactory;
import org.iq80.leveldb.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;

public class LevelDBBlockPersistence implements BlockPersistence {
    private static final Logger logger = LoggerFactory.getLogger(LevelDBBlockPersistence.class);
    private static final byte[] CHAIN_TIP_KEY = "chainTip".getBytes();

    private DB db;
    private ByteBuffer buf = ByteBuffer.allocate(StoredBlock.SIZE);
    private File path;

    /** Creates a LevelDB block store using the JNI/C++ version of LevelDB. */
    public LevelDBBlockPersistence(File directory) throws BlockPersistenceException {
        this(directory, JniDBFactory.factory);
    }

    /** Creates a LevelDB block store using the given factory */
    public LevelDBBlockPersistence(File directory, DBFactory dbFactory) throws BlockPersistenceException {
        this.path = directory;
        if (!path.exists()) {
            path.mkdir();
        }
        Options options = new Options();
        options.createIfMissing();

        try {
            tryOpen(directory, dbFactory, options);
        } catch (IOException | VerificationException e) {
            try {
                dbFactory.repair(directory, options);
                tryOpen(directory, dbFactory, options);
            } catch (IOException | VerificationException e1) {
                throw new BlockPersistenceException(e1);
            }
        }
    }

    /** try to open the leveldb files */
    private synchronized void tryOpen(File directory, DBFactory dbFactory, Options options) throws IOException, BlockPersistenceException, VerificationException {
        db = dbFactory.open(directory, options);
        initStoreIfNeeded();
    }

    /** try to init the db files if needed */
    private synchronized void initStoreIfNeeded() throws BlockPersistenceException, VerificationException {
        if (db.get(CHAIN_TIP_KEY) != null)
            return;   // Already initialised.
        Block genesis = NetworkParameters.getNetworkParameters().genesisBlock;
        StoredBlock storedGenesis = new StoredBlock(genesis, genesis.getWork(), 0);
        put(storedGenesis);
        setChainTip(storedGenesis);
    }

    @Override
    public synchronized void put(StoredBlock block) throws BlockPersistenceException {
        try {
            buf.clear();
            block.serialize(buf);
            db.put(block.getBlock().getHash().getBytes(), buf.array());
        } catch (IOException e) {
            throw new BlockPersistenceException(e);
        }
    }

    @Override
    public synchronized StoredBlock get(SHA256Hash hash) throws BlockPersistenceException {
        byte[] bytes = db.get(hash.getBytes());
        if (bytes == null)
            return null;
        return StoredBlock.deserialize(ByteBuffer.wrap(bytes));
    }

    @Override
    public synchronized StoredBlock getChainTip() throws BlockPersistenceException {
        return get(new SHA256Hash(db.get(CHAIN_TIP_KEY)));
    }

    @Override
    public synchronized void setChainTip(StoredBlock tip) throws BlockPersistenceException {
        db.put(CHAIN_TIP_KEY, tip.getBlock().getHash().getBytes());
    }

    @Override
    public synchronized void close() throws BlockPersistenceException {
        try {
            db.close();
        } catch (IOException e) {
            throw new BlockPersistenceException(e);
        }
    }

    /** Below for test */

    /** destroy the db file */
    public synchronized void destroy() throws IOException {
        JniDBFactory.factory.destroy(path, new Options());
    }

    /** Erases the contents of the database (but NOT the underlying files themselves) and then reinitialises with the genesis block. */
    public synchronized void reset() throws BlockPersistenceException {
        try {
            WriteBatch batch = db.createWriteBatch();
            try {
                DBIterator it = db.iterator();
                try {
                    it.seekToFirst();
                    while (it.hasNext())
                        batch.delete(it.next().getKey());
                    db.write(batch);
                } finally {
                    it.close();
                }
            } finally {
                batch.close();
            }
            initStoreIfNeeded();
        } catch (IOException | VerificationException e) {
            throw new BlockPersistenceException(e);
        }
    }
}
