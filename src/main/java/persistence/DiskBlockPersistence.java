/**
 * Created By Yufan Wu
 * 2019/3/26
 */
package persistence;

import core.SHA256Hash;
import core.StoredBlock;
import exception.BlockPersistenceException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.RandomAccessFile;
import java.nio.channels.FileChannel;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

public class DiskBlockPersistence implements BlockPersistence {
    private static final Logger logger = LoggerFactory.getLogger(DiskBlockPersistence.class);
    private static final byte FILE_FORMAT_VERSION = 1; // used for specified the format and version of the block file

    private RandomAccessFile file;

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

    private SHA256Hash chainTip;
    private FileChannel channel;

    private static class Record {
        private static final int CHAIN_WORK_BYTES = 16; // the number of byte used for storing total work
        private static final byte[] EMPTY_BYTES = new byte[CHAIN_WORK_BYTES]; // buffer hold byte 0
    }

    @Override
    public void put(StoredBlock block) throws BlockPersistenceException {

    }

    @Override
    public StoredBlock get(SHA256Hash hash) throws BlockPersistenceException {
        return null;
    }

    @Override
    public StoredBlock getChainTip() throws BlockPersistenceException {
        return null;
    }

    @Override
    public void setChainTip(StoredBlock block) throws BlockPersistenceException {

    }
}
