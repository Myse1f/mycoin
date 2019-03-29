/**
 * Created By Yufan Wu
 * 2019/3/26
 */
package persistence;

import core.Block;
import core.BlockHead;
import core.SHA256Hash;
import core.StoredBlock;
import exception.BlockPersistenceException;
import exception.ProtocolException;
import exception.VerificationException;
import net.NetworkParameters;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

/**
 * Stores the block chain to disk
 *
 * File Format --
 * 1 byte file format version
 * 32 bytes chain tip block hash
 * ... Block Record
 */
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

    /**
     * A Record represent a block data in disk file
     * It contains some method to read or write from the file
     */
    private static class Record {
        private static final int CHAIN_WORK_BYTES = 16; // the number of byte used for storing total work
        private static final byte[] EMPTY_BYTES = new byte[CHAIN_WORK_BYTES]; // buffer hold byte 0

        private int height; // 4 bytes
        private byte[] chainWork; // 16 bytes
        private byte[] blockHead; // 44 bytes

        public static final int SIZE = 4 + Record.CHAIN_WORK_BYTES + BlockHead.BLOCK_HEAD_SIZE;

        public Record() {
            height = 0;
            chainWork = new byte[CHAIN_WORK_BYTES];
            blockHead = new byte[BlockHead.BLOCK_HEAD_SIZE];
        }

        public static void write(FileChannel channel, StoredBlock block) throws IOException {
            // TODO add index using LevelDB
            ByteBuffer buf = ByteBuffer.allocate(Record.SIZE);
            buf.putInt(block.getHeight());
            byte[] chainWorkBytes = block.getChainWork().toByteArray();
            /** chainWork use a constant 16 bytes to store, thus padding with 0 if necessary */
            if (chainWorkBytes.length < CHAIN_WORK_BYTES) {
                buf.put(EMPTY_BYTES, 0, CHAIN_WORK_BYTES - chainWorkBytes.length);
            }
            buf.put(chainWorkBytes);
            buf.put(block.getBlock().serialize());
            buf.position(0);
            channel.position(channel.size());
            if (channel.write(buf) < Record.SIZE) {
                throw new IOException("Fail to write record!");
            }
            channel.position(channel.size() - Record.SIZE);
        }

        public boolean read(FileChannel channel, long position, ByteBuffer buffer) throws IOException {
            buffer.position(0);
            long len = channel.read(buffer, position);
            if (len < Record.SIZE) {
                return false;
            }
            buffer.position(0);
            height = buffer.getInt();
            buffer.get(chainWork);
            buffer.get(blockHead);
            return true;
        }

        public BigInteger getChainWork() {
            return new BigInteger(1, chainWork);
        }

        public BlockHead getHead() {
            return BlockHead.deserialize(blockHead);
        }

        public int getHeight() {
            return height;
        }

        public StoredBlock toStoredBlock() {
            return new StoredBlock(new Block(getHead()), getChainWork(), getHeight());
        }
    }

    public DiskBlockPersistence(File file) throws BlockPersistenceException {
        if (file.exists()) {
            try {
                load(file);
                return;
            } catch (Exception e) {
                logger.error("Failed to load block chain from " + file, e);
            }
        }

        createNewStore(file);
    }

    private void createNewStore(File file) throws BlockPersistenceException {
        blockCache.clear();
        try {
            if (file.exists()) {
                if (!file.delete()) {
                    throw new BlockPersistenceException("Can't delete old storage file in order to recreate it!");
                }
            }
            //recreate
            this.file = new RandomAccessFile(file, "rwd");
            this.channel = this.file.getChannel();
            this.file.write(FILE_FORMAT_VERSION);
        } catch (IOException e) {
            throw new BlockPersistenceException(e);
        }
        try {
            Block genesis = NetworkParameters.getNetworkParameters().genesisBlock;
            StoredBlock storedBlock = new StoredBlock(genesis, genesis.getWork(), 0);
            this.chainTip = genesis.getHash();
            this.file.write(this.chainTip.getBytes());
            put(storedBlock);
        } catch (VerificationException e) {
            throw new RuntimeException(e); //impossible
        } catch (IOException e) {
            throw new BlockPersistenceException(e);
        }
    }

    private void load(File file) throws IOException, BlockPersistenceException {
        logger.info("Loading block from {}", file);
        this.file = new RandomAccessFile(file, "rwd");
        try {
            channel = this.file.getChannel();
            //read version byte
            int version = this.file.read();
            if (version == -1) {
                throw new FileNotFoundException(file.getName() + "dosen't exist or is empty!");
            }
            if (version != FILE_FORMAT_VERSION) {
                throw new BlockPersistenceException("Bad version byte:" + version);
            }
            // read chain tip hash
            byte[] chainTipHash = new byte[32];
            if (this.file.read(chainTipHash) < chainTipHash.length) {
                throw new BlockPersistenceException("Could not read chain tip hash!");
            }
            this.chainTip = new SHA256Hash(chainTipHash);
            logger.info("Read chain tip from disk file: {}", this.chainTip);
            channel.position(channel.size() - Record.SIZE);
        } catch (Exception e) {
            this.file.close();
            throw e;
        }
    }

    @Override
    public synchronized void put(StoredBlock block) throws BlockPersistenceException {
        try {
            SHA256Hash hash = block.getBlock().getHash();
            // Append to the end of the file.
            Record.write(channel, block);
            blockCache.put(hash, block);
        } catch (IOException e) {
            throw new BlockPersistenceException(e);
        }
    }

    @Override
    public synchronized StoredBlock get(SHA256Hash hash) throws BlockPersistenceException {
        // Check the memory cache first.
        StoredBlock fromMem = blockCache.get(hash);
        if (fromMem != null) {
            return fromMem;
        }
        if (notFoundCache.contains(hash)) {
            return null;
        }

        try {
            Record fromDisk = getRecord(hash);
            StoredBlock block = null;
            if (fromDisk == null) {
                notFoundCache.add(hash);
            } else {
                block = fromDisk.toStoredBlock();
                blockCache.put(hash, block);
            }
            return block;
        } catch (IOException e) {
            throw new BlockPersistenceException(e);
        }
    }

    private ByteBuffer buf = ByteBuffer.allocateDirect(Record.SIZE);

    private Record getRecord(SHA256Hash hash) throws IOException {
        long startPos = channel.position();
        // Use our own file pointer within the tight loop as updating channel positions is really expensive.
        long pos = startPos;
        Record record = new Record();
        do {
            if (!record.read(channel, pos, buf))
                throw new IOException("Failed to read buffer");
            if (new Block(record.getHead()).getHash().equals(hash)) {
                // Found it. Update file position for next time.
                channel.position(pos);
                return record;
            }
            // Did not find it.
            if (pos == 1 + 32) {
                // At the start so wrap around to the end.
                pos = channel.size() - Record.SIZE;
            } else {
                // Move backwards.
                pos = pos - Record.SIZE;
                assert pos >= 1 + 32 : pos;
            }
        } while (pos != startPos);
        // Was never stored.
        channel.position(pos);
        return null;
    }

    @Override
    public synchronized StoredBlock getChainTip() throws BlockPersistenceException {
        StoredBlock head = get(chainTip);
        if (head == null)
            throw new BlockPersistenceException("Corrupted block store: chain tip not found");
        return head;
    }

    @Override
    public synchronized void setChainTip(StoredBlock block) throws BlockPersistenceException {
        try {
            this.chainTip = block.getBlock().getHash();
            channel.write(ByteBuffer.wrap(this.chainTip.getBytes()), 1);
        } catch (IOException e) {
            throw new BlockPersistenceException(e);
        }
    }

    @Override
    public void close() throws BlockPersistenceException {
        blockCache.clear();
        notFoundCache.clear();
        try {
            this.file.close();
        } catch (IOException e) {
            throw new BlockPersistenceException(e);
        }
    }
}
