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
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

/**
 * LevelDB based block persistence
 * Use memory to cache recently used blocks
 */
public class LevelDBBlockPersistence implements BlockPersistence {
    private static final Logger logger = LoggerFactory.getLogger(LevelDBBlockPersistence.class);
    private static final byte[] CHAIN_TIP_KEY = "chainTip".getBytes();

    private DB db;
    private ByteBuffer buf = ByteBuffer.allocate(StoredBlock.SIZE);
    private File path;

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

    /**
     * Keep the cache of not found block to track get() miss
     */
    private Set<SHA256Hash> notFoundCache = Collections.newSetFromMap(new LinkedHashMap<SHA256Hash, Boolean>() {
        @Override
        protected boolean removeEldestEntry(Map.Entry<SHA256Hash, Boolean> entry) {
            return size() > 100;
        }
    });

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
        if (notFoundCache.contains(hash)) {
            return null;
        }
        byte[] bytes = db.get(hash.getBytes());
        if (bytes == null) {
            notFoundCache.add(hash);
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
            notFoundCache.clear();
            blockCache.clear();
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
        blockCache.clear();
        notFoundCache.clear();
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