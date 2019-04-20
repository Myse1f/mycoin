/**
 * Created By Yufan Wu
 * 2018/12/25
 */
package core;

import exception.BlockPersistenceException;
import exception.PeerException;
import exception.ProtocolException;
import exception.VerificationException;
import net.NetworkConnection;
import net.NetworkParameters;
import net.PeerAddress;
import net.TCPNetworkConnection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import persistence.BlockPersistence;
import utils.EventListenerInvoker;
import utils.SpringContextUtil;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * A {@link Peer} handlers the high level communication with other nodes
 * When connect() successfully, call run() to start message loop
 */
public class Peer {
    private static final Logger logger = LoggerFactory.getLogger(Peer.class);
    public static final int CONNECT_TIMEOUT_MSEC = 60000;

    private NetworkConnection connection;
    private boolean inBound; // whether this peer is connect inbound or outbound
    private boolean running;
    private PeerAddress address;
    private final BlockChain blockChain;

    private Set<Inv> invKonwn = new HashSet<>();
    private List<PeerEventListener> eventListeners;

    private int versionHeight = -1; // height in version message from this peer

    /**
     * construct a peer that uses peer address, then call connect() to connect to it
     */
    public Peer(PeerAddress address, BlockChain blockChain) {
        this.blockChain = blockChain;
        this.address = address;
        this.inBound = false; // this construction means this connection is outBound
        eventListeners = new ArrayList<>();
    }

    /**
     * Construct a peer that uses the given, already connected network connection object.
     */
    public Peer(BlockChain blockChain, NetworkConnection connection) {
        this(null, blockChain);
        this.connection = connection;
        this.inBound = true; // this construction means this connection is inBound
        this.address = connection.getPeerAddress();
    }

    /**
     * add a inv into known inventory
     * @param inv
     */
    public void pushInvKnown(Inv inv) {
        invKonwn.add(inv);
    }

    /**
     * if a inv is know by this peer
     */
    public boolean isInvKown(Inv inv) {
        return invKonwn.contains(inv);
    }

    /**
     * for test
     */
    public void setConnection(NetworkConnection connection) {
        this.connection = connection;
    }

    public void connect() throws PeerException {
        try {
            connection = new TCPNetworkConnection(address, CONNECT_TIMEOUT_MSEC, createVersionMessage());
        } catch (IOException e) {
            throw new PeerException(e);
        }
    }

    /**
     * Runs in the peer network and manage communication with others peers
     * connect() must be called first
     */
    public void run() throws PeerException {
        if (connection == null) {
           throw new RuntimeException("Connect() must be called first!");
        }

        running = true;

        try {
            while (true) {
                Message m = connection.readMessage();
                String cmd = m.getCommand();
                {
                    if(cmd.equals(MessageHeader.VERSION)) {
                        processVersionMessage(m);
                    } else if(cmd.equals(MessageHeader.VERBACK)) {
                        processVerbackMessage();
                    } else if (versionHeight <= 0) {
                        throw new ProtocolException("Version height need to be set before further communication.");
                    } else if(cmd.equals(MessageHeader.INV)) {
                        processInvMessgae(m);
                    } else if(cmd.equals(MessageHeader.BLOCK)) {
                        processBlockMessage(m);
                    } else if(cmd.equals(MessageHeader.GETBLOCKS)){
                        processGetBlocksMessage(m);
                    } else if(cmd.equals(MessageHeader.GETDATA)) {
                        processGetDataMessage(m);
                    } else {
                        logger.info("Received unsupported message: {}", m);
                    }
                }
            }
        } catch (ProtocolException|ClassNotFoundException|BlockPersistenceException e) {
            disconnect();
            throw new PeerException(e);
        } catch (IOException e) {
            if (!running) {
                // This exception was expected because we are tearing down the socket as part of quitting.
                logger.info("Shutting down peer loop");
            } else {
                disconnect();
                throw new PeerException(e);
            }
        } catch (RuntimeException e) {
            disconnect();
            logger.error("unexpected exception in peer loop: ", e.getMessage());
            throw e;
        }

        disconnect();
    }

    private void processVersionMessage(Message msg) throws IOException, ClassNotFoundException {
        logger.debug("Received version message.");
        versionHeight = msg.getPayloadAsInteger();
        sendMessage(createVerbackMessage());
        // this connection is inbound, need to send a version message back
        if (inBound) {
            sendMessage(createVersionMessage());
        }
    }

    private void processVerbackMessage() {
        logger.info("Connect to peer: {}", this);
    }

    private void processInvMessgae(Message msg) throws IOException, ClassNotFoundException, BlockPersistenceException {
        logger.debug("Received Inv message.");
        List<Inv> invs = msg.getPayloadAsInvs();
        List<Inv> getDataInvs = new ArrayList<>();
        for (Inv inv: invs) {
            pushInvKnown(inv); // this peer has these inventory
            if (inv.getType() == Inv.InvType.MSG_BLOCK) {
                if (!blockChain.hasBlock(inv.getHash())) {
                    getDataInvs.add(inv);
                }
            } else {
                // further improvement
            }
        }
        Message getDataMessage = createGetDataMessage(getDataInvs);
        sendMessage(getDataMessage);
    }

    private void processBlockMessage(Message msg) throws IOException, ClassNotFoundException, PeerException, BlockPersistenceException {
        Block block = msg.getPayloadAsBlock();
        logger.debug("Received block message containing {}", block);
        try {
            if (blockChain.add(block)) {
                invokeOnBlocksDownloaded(block);
            } else {
                blocksDownload(block.getHash());
            }
        } catch (VerificationException e) {
            logger.warn("Block validate failure, {}", block);
        }
    }

    private void processGetBlocksMessage(Message msg) throws IOException, ClassNotFoundException, BlockPersistenceException {
        logger.debug("Received getBlocks message.");
        Object[] obj = msg.getPayloadAsLocatorAndHash();
        SHA256Hash[] locator = (SHA256Hash[]) obj[0];
        SHA256Hash hashStop = (SHA256Hash) obj[1];
        StoredBlock locatedBlock = null;
        for (SHA256Hash hash : locator) {
            // find the block where we first have
            if (blockChain.hasBlock(hash)) {
                locatedBlock = blockChain.getBlockPersistence().get(hash);
                break;
            }
        }
        // send inv message with block's hash from locator to hashStop
        assert locatedBlock != null;
        StoredBlock cursor = locatedBlock.getNextBlock(blockChain.getBlockPersistence());
        List<Inv> invs = new ArrayList<>();
        while(cursor != null && !cursor.getBlock().getHash().equals(hashStop)) {
            invs.add(new Inv(Inv.InvType.MSG_BLOCK, cursor.getBlock().getHash())); //todo make a limit in case that requested blocks is too large
            cursor = cursor.getNextBlock(blockChain.getBlockPersistence());
        }
        if (!invs.isEmpty()) {
            sendMessage(createInvMessage(invs));
        }
    }

    private void processGetDataMessage(Message msg) throws IOException, ClassNotFoundException {
        logger.debug("Received getdata message.");
        List<Message> requestData = new ArrayList<>();
        for (PeerEventListener listener : eventListeners) {
            synchronized (listener) {
                List<Message> data = listener.getData(this, msg);
                if (data.isEmpty()) {
                    continue;
                }
                requestData.addAll(data);
            }
        }
        if (requestData.isEmpty()) {
            return; // nothing I have
        }
        logger.debug("Sending {} items to peer {}", requestData.size(), this);
        for (Message m : requestData) {
            sendMessage(m);
        }
    }

    /**
     * Stop the peer network and disconnect from remote peer
     */
    public void disconnect() {
        running = false;
        try {
            // This is the correct way to stop an IO bound loop
            if (connection != null)
                connection.shutdown();
        } catch (IOException e) {
            // Don't care about this.
        }
    }


    public synchronized void addEventListener(PeerEventListener listener) {
        eventListeners.add(listener);
    }

    public synchronized boolean removeEventListener(PeerEventListener listener) {
        return eventListeners.remove(listener);
    }

    /**
     * get the height difference between this peer and ours
     */
    public int getChainHeightDifference() throws PeerException {
        if (versionHeight <= 0) {
            throw new PeerException("Connected to a peer with zero/negative chain height.");
        }
        return versionHeight - blockChain.getChainHeight();
    }

    private void invokeOnBlocksDownloaded(final Block m) throws PeerException {
        // It is possible for the peer block height difference to be negative when blocks have been solved and broadcast
        // since the time we first connected to the peer. However, it's weird and unexpected to receive a callback
        // with negative "blocks left" in this case, so we clamp to zero so the API user doesn't have to think about it.
        final int blocksLeft = Math.max(0, getChainHeightDifference());
        EventListenerInvoker.invoke(eventListeners, new EventListenerInvoker<PeerEventListener>() {
            @Override
            public void invoke(PeerEventListener listener) {
                listener.onBlockDownloaded(Peer.this, m, blocksLeft);
            }
        });
    }

    /**
     * Start blocks download from this peer. We need height to get the blocks number to download
     * @throws IOException
     */
    public void startBlocksDownload() throws IOException, PeerException {
        if (getChainHeightDifference() >= 0) {
            EventListenerInvoker.invoke(eventListeners, new EventListenerInvoker<PeerEventListener>() {
                @Override
                public void invoke(PeerEventListener listener) {
                    try {
                        listener.onChainDownloadStart(Peer.this, getChainHeightDifference());
                    } catch (PeerException e) {
                        // can't be here, it will be caught before
                    }
                }
            });
        }

        // start, get as many blocks as possible
        blocksDownload(SHA256Hash.ZERO_HASH);
    }

    /**
     * get blocks from this peer
     * from ours chain tip to the hashStop
     * @param hashStop
     */
    private void blocksDownload(SHA256Hash hashStop) throws IOException {
        logger.info("blocksDownload({}", hashStop.toString());
        BlockPersistence source = blockChain.getBlockPersistence();
        StoredBlock cursor = blockChain.getChainTip();
        /**
         * Construct block locator which indicate the top 50 blocks and the genesis block
         * It means that we assume that the block difference is less than 50
         */
        List<SHA256Hash> blockLocator = new ArrayList<>(51);
        for (int i = 50; cursor != null && i > 0; i--) {
            blockLocator.add(cursor.getBlock().getHash());
            try {
                cursor = cursor.getPreviousBlock(source);
            } catch (BlockPersistenceException e) {
                logger.error("Fail to trace the block chain while constructing a locator.");
                throw new RuntimeException(e);
            }
        }
        if (cursor != null) {
            blockLocator.add(((NetworkParameters)(SpringContextUtil.getBean("network_params"))).genesisBlock.getHash());
        }

        Message getBlocksMessage = createGetBlocksMessage(blockLocator, hashStop);
        sendMessage(getBlocksMessage);
    }

    private Message createVerbackMessage() {
        return new Message(MessageHeader.VERBACK, 12 + MessageHeader.SIZE, "I'm verback".getBytes());
    }

    private Message createVersionMessage() throws IOException {
        Message versionMessage = new Message(MessageHeader.VERSION, 0, null);
        byte[] payload = Utils.objectsToByteArray(
                new Message(MessageHeader.VERSION, Integer.BYTES + MessageHeader.SIZE, Utils.objectsToByteArray(blockChain.chainTip.getHeight()))
        );
        versionMessage.setMessageSize(payload.length);
        versionMessage.setPayload(payload);
        return versionMessage;
    }

    private Message createGetBlocksMessage(List<SHA256Hash> locator, SHA256Hash hashStop) throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(bos);
        byte[] payload;
        SHA256Hash[] locatorArray = new SHA256Hash[locator.size()];
        locator.toArray(locatorArray);
        oos.writeObject(locatorArray);
        oos.writeObject(hashStop);
        oos.flush();
        payload = bos.toByteArray();
        oos.close();
        bos.close();
        return new Message(MessageHeader.GETBLOCKS, MessageHeader.SIZE + payload.length, payload);
    }

    private Message createGetDataMessage(List<Inv> invs) throws IOException {
        Message msg = new Message(MessageHeader.GETDATA, 0, null);
        msg.setInvsIntoPayload(invs);
        msg.setMessageSize(MessageHeader.SIZE + msg.getPayload().length);
        return msg;
    }

    private Message createInvMessage(List<Inv> invs) throws IOException {
        Message msg = new Message(MessageHeader.INV, 0 , null);
        msg.setInvsIntoPayload(invs);
        msg.setMessageSize(MessageHeader.SIZE + msg.getPayload().length);
        return msg;
    }

    /**
     * send a message to this peer, using the connection
     * @param msg message
     * @throws IOException
     */
    public void sendMessage(Message msg) throws IOException {
        connection.writeMessage(msg);
    }

    @Override
    public String toString() {
        if (address == null) {
            // User-provided NetworkConnection object.
            return "Peer(NetworkConnection:" + connection + ")";
        } else {
            return "Peer(" + address.getAddr().getHostAddress() + ":" + address.getPort() + ")";
        }
    }

    public boolean isRunning() {
        return running;
    }

    public PeerAddress getAddress() {
        return address;
    }
}
