/**
 * Created By Yufan Wu
 * 2018/12/25
 */
package core;

import exception.PeerException;
import exception.ProtocolException;
import net.NetworkConnection;
import net.NetworkParameters;
import net.PeerAddress;
import net.TCPNetworkConnection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
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

    private boolean running;
    private PeerAddress address;
    private final BlockChain blockChain;

    private Set<Inv> invKonwn = new HashSet<>();
    private List<PeerEventListener> eventListeners;

    public Peer(PeerAddress address, BlockChain blockChain) {
        this.blockChain = blockChain;
        this.address = address;
    }

    /**
     * Construct a peer that uses the given, already connected network connection object.
     */
    public Peer(BlockChain blockChain, NetworkConnection connection) {
        this(null, blockChain);
        this.connection = connection;
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
            connection = new TCPNetworkConnection(address, CONNECT_TIMEOUT_MSEC);
        } catch (ProtocolException | IOException e) {
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
                switch (m.getCommand()) {
                    // TODO process message
                    case MessageHeader.VERSION: break;
                    case MessageHeader.VERBACK: break;
                    case MessageHeader.INV: break;
                    case MessageHeader.BLOCK: break;
                    case MessageHeader.GETBLOCKS: break;
                    case MessageHeader.GETDATA: break;
                    default: break;
                }
            }
        } catch (ProtocolException e) {
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

    @Override
    public String toString() {
        if (address == null) {
            // User-provided NetworkConnection object.
            return "Peer(NetworkConnection:" + connection + ")";
        } else {
            return "Peer(" + address.getAddr() + ":" + address.getPort() + ")";
        }
    }

    public synchronized void addEventListener(PeerEventListener listener) {
        eventListeners.add(listener);
    }

    public synchronized boolean removeEventListener(PeerEventListener listener) {
        return eventListeners.remove(listener);
    }

    // TODO
    public void startBlocksDownload() throws IOException{
    }

    // TODO
    public void sendMessage(Message msg) throws IOException {
        connection.writeMessage(msg);
    }
}
