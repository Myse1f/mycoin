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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * LevelDB based block persistence
 * Use memory to cache recently used blocks
 */
@Component
public class LevelDBBlockPersistence implements BlockPersistence {
    private static final Logger logger = LoggerFactory.getLogger(LevelDBBlockPersistence.class);
    private static final byte[] CHAIN_TIP_KEY = "chainTip".getBytes();

    private NetworkParameters params;

    private DB db;
    private ByteBuffer buf = ByteBuffer.allocate(StoredBlock.SIZE);
    private static File path = new File("data");

    /**
     * Keep the cache of block into memory for up to 2050 blocks. It can help to optimize some cases where we are looking up
     * recent blocks.
     */
    private LinkedHashMap<SHA256Hash, StoredBlock> blockCache = new LinkedHashMap<SHA256Hash, StoredBlock>() {
        @Override
        protected boolean removeEldestEntry(Map.Entry<SHA256Hash, StoredBlock> entry) {
            return size() > 2050;
        }
    };

    /** Creates a LevelDB block store using the given factory */
    @Autowired
    public LevelDBBlockPersistence(NetworkParameters params) throws BlockPersistenceException {
        this.params = params;
        DBFactory dbFactory = JniDBFactory.factory;
        if (!path.exists()) {
            path.mkdir();
        }
        Options options = new Options();
        options.createIfMissing();

        try {
            tryOpen(path, dbFactory, options);
        } catch (IOException | VerificationException e) {
            try {
                dbFactory.repair(path, options);
                tryOpen(path, dbFactory, options);
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
        Block genesis = params.genesisBlock;
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
            blockCache.put(block.getBlock().getHash(), block);
        } catch (IOException e) {
            throw new BlockPersistenceException(e);
        }
    }

    @Override
    public synchronized StoredBlock get(SHA256Hash hash) throws BlockPersistenceException {
        StoredBlock fromeMem = blockCache.get(hash);
        if (fromeMem != null) {
            return fromeMem;
        }
        byte[] bytes = db.get(hash.getBytes());
        if (bytes == null) {
            return null;
        }
        StoredBlock blockFound = StoredBlock.deserialize(ByteBuffer.wrap(bytes));
        blockCache.put(hash, blockFound);
        return blockFound;
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
            blockCache.clear();
            db.close();
        } catch (IOException e) {
            throw new BlockPersistenceException(e);
        }
    }

    /** Below for test */

    public LevelDBBlockPersistence(NetworkParameters params, File file) throws BlockPersistenceException {
        this.params = params;
        DBFactory dbFactory = JniDBFactory.factory;
        Options options = new Options();
        options.createIfMissing();

        try {
            tryOpen(file, dbFactory, options);
        } catch (IOException | VerificationException e) {
            try {
                dbFactory.repair(file, options);
                tryOpen(file, dbFactory, options);
            } catch (IOException | VerificationException e1) {
                throw new BlockPersistenceException(e1);
            }
        }
    }

    /** destroy the db file */
    public synchronized void destroy() throws IOException {
        JniDBFactory.factory.destroy(path, new Options());
    }

    /** Erases the contents of the database (but NOT the underlying files themselves) and then reinitialises with the genesis block. */
    public synchronized void reset() throws BlockPersistenceException {
        blockCache.clear();
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
