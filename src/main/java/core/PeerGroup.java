/**
 * Created By Yufan Wu
 * 2019/3/29
 */
package core;

import exception.BlockPersistenceException;
import exception.PeerException;
import net.NetworkParameters;
import net.PeerAddress;
import net.TCPNetworkConnection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import utils.EventListenerInvoker;

import java.io.IOException;
import java.net.ConnectException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Maintain a list of peers. Manage the connection between peers.
 */
public class PeerGroup {
    private static final Logger logger = LoggerFactory.getLogger(PeerGroup.class);
    private static final int DEAFAULT_CONNECTIONS = 4; // limited by my machines number
    private static final int THREAD_KEEP_ALIVE_SECONDS = 1;

    private static PeerGroup peerGroup; //singleton

    private Set<Peer> peers; // connected peers
    private Peer downloadPeer; // the peer where we
    private BlockChain blockchain;
    private boolean running;
    private ThreadPoolExecutor peerPool; // thread pool for peers
    private PeerGroupThread peerGroupThread;
    private PeerEventListener getDataListener;
    private List<PeerEventListener> peerEventListeners;
    private PeerEventListener downloadListener;

    /** can only be run once at the beginning */
    public static void init(BlockChain chain) {
        peerGroup = new PeerGroup(chain);
    }

    private PeerGroup(BlockChain chain) {
        this.blockchain = chain;
        this.peers = Collections.synchronizedSet(new HashSet<>());
        this.peerEventListeners = new ArrayList<>();
        this.peerPool = new ThreadPoolExecutor(DEAFAULT_CONNECTIONS, DEAFAULT_CONNECTIONS, THREAD_KEEP_ALIVE_SECONDS, TimeUnit.SECONDS, new LinkedBlockingQueue<Runnable>(1), new PeerThreadFactory());

        getDataListener = new AbstractPeerEventListener() {
            @Override
            public List<Message> getData(Peer peer, Message m) {
                return processGetData(m);
            }
        };

        downloadListener = new DownloadListener() {
            @Override
            public void onBlockDownloaded(Peer peer, Block block, int blocksLeft) {
                super.onBlockDownloaded(peer, block, blocksLeft);
                // broadcast block inv to all peers
                Inv blockInv = new Inv(Inv.InvType.MSG_BLOCK, block.getHash());
                try {
                    brocastBlcokInv(blockInv);
                } catch (IOException e) {
                    logger.error("Fail to broadcast Block Inv {}", blockInv);
                }
            }
        };
    }

    /**
     * process the getdata message, this version only request for block data
     * @param m inv message
     * @return a message contains request data
     */
    private synchronized List<Message> processGetData(Message m) {
        List<Message> msgs = new LinkedList<>();
        try {
            List<Inv> invs = m.getPayloadAsInvs();
            for (Inv inv : invs) {
                if (inv.getType() != Inv.InvType.MSG_BLOCK) {
                    logger.debug("Inv message contains non-block inventory, which is impossible in this version.");
                    continue;
                }
                if (!blockchain.hasBlock(inv.getHash())) {
                    logger.info("Database has no block : {}", inv.getHash());
                    continue;
                }
                StoredBlock block = blockchain.getBlockPersistence().get(inv.getHash());
                msgs.add(new Message("block", 0, Utils.objectsToByteArray(block.getBlock())));
            }
        } catch (IOException | ClassNotFoundException e) {
            logger.error("{}", e);
        } catch (BlockPersistenceException e) {
            logger.error("Can't access block database.", e);
        }
        return msgs;
    }

    public static synchronized PeerGroup getInstance() {
        if (peerGroup == null) {
            throw new RuntimeException("PeerGroup is not initialized.");
        }
        return peerGroup;
    }

    /**
     * add a listener into list
     * the listener will be locked during callback execution, which in turn will cause network message processing to stop until the listener returns
     */
    public synchronized void addEventListener(PeerEventListener listener) {
        peerEventListeners.add(listener);
    }

    /** remove a specified listener from the listener list */
    public synchronized boolean removeEventListener(PeerEventListener listener) {
        return peerEventListeners.remove(listener);
    }


    /**
     * add a peer into peer group, then run it immediately,
     * The size of the peers pool will be managed out of it
     * @param peer
     */
    public void addPeer(Peer peer) {
        synchronized (this) {
            if (!running) {
                throw new IllegalStateException("Must call start() before adding peers.");
            }
            logger.info("Add peer to group: {}", peer);
        }

        runNewPeer(peer, true);
    }

    /**
     * Returns a newly allocated list containing the currently connected peers.
     */
    public synchronized List<Peer> getConnectedPeers() {
        ArrayList<Peer> result = new ArrayList<>(peers.size());
        result.addAll(peers);
        return result;
    }

    public synchronized void start() throws IOException {
        this.peerGroupThread = new PeerGroupThread();
        this.running = true;
        this.peerGroupThread.start();
        logger.info("Peer network start working.");
    }

    public synchronized void stop() throws IOException {
        if (running) {
            running = false;
            this.peerGroupThread.shutdown();
            logger.info("Stop running peer group.");
        }
    }

    public synchronized boolean isRunning() {
        return running;
    }

    /**
     * run a new peer in a thread
     * the peer may be created from serverSocket thus need not to connect
     * if connect is true which means connect to it actively, then connect to the peer
     * @param peer
     */
    public void runNewPeer(final Peer peer, boolean connect) {
        peerPool.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    if (connect) {
                        logger.info("Connecting to peer {}", peer);
                        peer.connect();
                    }
                    synchronized (PeerGroup.this) {
                        if (!running) {
                            peer.disconnect();
                            return;
                        }
                        peers.add(peer);
                    }
                    handleNewPeer(peer);
                    peer.run(); // infinite loop here, process message
                } catch (PeerException e) {
                    final Throwable cause = e.getCause();
                    if (cause instanceof SocketTimeoutException) {
                        logger.info("Timeout talking to " + peer + ": " + cause.getMessage());
                    } else if (cause instanceof ConnectException) {
                        logger.info("Could not connect to " + peer + ": " + cause.getMessage());
                    } else if (cause instanceof IOException) {
                        logger.info("Error talking to " + peer + ": " + cause.getMessage());
                    } else {
                        logger.error("Unexpected exception whilst talking to " + peer, e);
                    }
                } finally {
                    // handle peer death here
                    synchronized (PeerGroup.this) {
                        if (!running) {
                            return; // the peer group is stop running
                        }
                        peer.disconnect();
                        peers.remove(peer);
                    }
                    handlePeerDeath(peer);
                }
            }
        });
    }



    /**
     * Download the blockchain from peers.
     * This method waits until the download is complete.  "Complete" is defined as downloading
     * from at least one peer all the blocks that are in that peer's inventory.
     */
    private synchronized void initialBlocksDownload() {
        synchronized (peers) {
            if (!peers.isEmpty()) {
                downloadFromPeer(peers.iterator().next());
            }
        }
    }

    /**
     * start download blocks from specific peer
     */
    private void downloadFromPeer(Peer peer) {
        try{
            peer.addEventListener(downloadListener);
            setDownloadPeer(peer);
            peer.startBlocksDownload();
        } catch (IOException e) {
            logger.error("Fail to start blocks download from peer {}", peer);
        } catch (PeerException e) {
            logger.error("Peer has some error, {}", e);
        }
    }

    /**
     * broadcast block Inv to all peers
     */
    public void brocastBlcokInv(Inv blockInv) throws IOException {
        synchronized (peers) {
            for (Peer peer : peers) {
                if (!peer.isInvKown(blockInv)) {
                    peer.sendMessage(new Message("inv", 0, Utils.objectsToByteArray(blockInv)));
                }
            }
        }
    }

    /**
     * handle peer death, if the peer is download peer, we need re-pick a new one
     * @param peer death peer
     */
    private synchronized void handlePeerDeath(Peer peer) {
        if (!isRunning()) {
            logger.info("Peer death while shutting down");
            return;
        }
        if (peer == downloadPeer) {
            logger.info("Download peer died. Picking a new one.");
            setDownloadPeer(null);
            synchronized (peers) {
                if (!peers.isEmpty()) {
                    Peer next = peers.iterator().next();
                    setDownloadPeer(next);
                    if (downloadListener != null) {
                        downloadFromPeer(next);
                    }
                }
            }
        }
        peer.removeEventListener(getDataListener);
        EventListenerInvoker.invoke(peerEventListeners, new EventListenerInvoker<PeerEventListener>() {
            @Override
            public void invoke(PeerEventListener listener) {
                listener.onPeerDisconnected(peer, peers.size());
            }
        });
    }

    private synchronized void setDownloadPeer(Peer peer) {
        if (downloadPeer != null) {
            logger.info("Unsetting download peer: {}", downloadPeer);
            downloadPeer.removeEventListener(downloadListener);
        }
        downloadPeer = peer;
        if (downloadPeer != null) {
            logger.info("Setting download peer: {}", downloadPeer);

        }
    }

    /**
     * handle a new connected peer
     * add getData listener and download data from it if necessary
     * @param peer
     */
    private synchronized void handleNewPeer(Peer peer) {
        logger.info("Handing new peer {}", peer);
        // download the chain if we never do it
        if (downloadListener != null && downloadPeer == null) {
            logger.info("   starting downloading blocks.");
            downloadFromPeer(peer);
        } else if (downloadPeer == null) {
            setDownloadPeer(peer);
        }
        peer.addEventListener(getDataListener);
        EventListenerInvoker.invoke(peerEventListeners, new EventListenerInvoker<PeerEventListener>() {
            @Override
            public void invoke(PeerEventListener listener) {
                listener.onPeerConnected(peer, peers.size());
            }
        });

    }

    /**
     * peer group thread for listening socket and wait for peers to connect
     */
    private final class PeerGroupThread extends Thread {
        ServerSocket serverSocket;

        public PeerGroupThread() throws IOException {
            super("PeerGroup Thread");
            serverSocket = new ServerSocket(NetworkParameters.getNetworkParameters().port);
            setPriority(Math.max(Thread.MIN_PRIORITY, Thread.currentThread().getPriority()-1));
            setDaemon(true);
        }

        @Override
        public void run() {
            logger.info("Listening in {}:{}", serverSocket.getInetAddress(), serverSocket.getLocalPort());
            try {
                while (isRunning()) {
                    Socket socket = serverSocket.accept(); // block here to wait for a new inbound connection
                    Peer peer = new Peer(blockchain, new TCPNetworkConnection(socket));
                    runNewPeer(peer, false);
                }
            } catch (IOException e) {
                // close server socket, stop this thread
            }

            // close all peer connection
            synchronized (PeerGroup.this) {
                running = false;
                peerPool.shutdown();
                synchronized (peers) {
                    for (Peer peer : peers) {
                        peer.disconnect();
                    }
                }
            }
        }

        public void shutdown() throws IOException {
            serverSocket.close();
        }
    }

    private static class PeerThreadFactory implements ThreadFactory {
        static final AtomicInteger poolNumber = new AtomicInteger(1);
        final ThreadGroup group;
        final AtomicInteger threadNumber = new AtomicInteger(1);
        final String namePrefix;

        public PeerThreadFactory() {
            group = Thread.currentThread().getThreadGroup();
            namePrefix = "PeerGroup-" + poolNumber.getAndIncrement() + "-thread-";
        }

        @Override
        public Thread newThread(Runnable r) {
            Thread t = new Thread(group, r, namePrefix + threadNumber.getAndIncrement(), 0);
            t.setPriority(Math.max(Thread.MIN_PRIORITY, Thread.currentThread().getPriority() - 1));
            t.setDaemon(true);
            return t;
        }
    }
}
