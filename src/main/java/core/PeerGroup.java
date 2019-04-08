/**
 * Created By Yufan Wu
 * 2019/3/29
 */
package core;

import net.PeerAddress;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.FutureTask;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * Maintain a list of peers. Manage the connection between peers.
 */
public class PeerGroup {
    private static final Logger logger = LoggerFactory.getLogger(PeerGroup.class);
    private static final int DEAFAULT_CONNECTIONS = 2; // limited by my machines number
    private static final int DEAFAULT_CONNECTION_DELAY_MILLIS = 5000;
    private static final int THREAD_KEEP_ALIVE_SECONDS = 1;

    private static PeerGroup peerGroup; //singleton

    private Set<Peer> peers; // connected peers
    private Peer downloadPeer; // the peer where we
    private BlockChain blockchain;
    private int connectionDelayMillis;

    private PeerEventListener getDataListener;
    private List<PeerEventListener> peerEventListeners;
    private PeerEventListener downloadListener;

    private PeerGroup(BlockChain chain) {
        this.blockchain = chain;
        this.connectionDelayMillis = DEAFAULT_CONNECTION_DELAY_MILLIS;
        this.peers = Collections.synchronizedSet(new HashSet<>());
        this.peerEventListeners = new ArrayList<>();

        getDataListener = new AbstractPeerEventListener() {
            @Override
            public List<Message> getData(Peer peer, Message m) {
                //TODO
                return null;
            }
        };
    }

    /**
     * process the getdata message, this version only request for block data
     * @param m inv message
     * @return a message contains request data
     */
    private synchronized List<Message> processGetData(Message m) {
        // TODO
        return null;
    }

    public synchronized PeerGroup getInstance(BlockChain chain) {
        if (peerGroup == null) {
            peerGroup = new PeerGroup(chain);
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

}
