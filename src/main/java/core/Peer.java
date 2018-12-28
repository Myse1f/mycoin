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
import java.util.Set;

/**
 * A {@code Peer} handlers the high level communication with other nodes
 * When connect() successfully, call run() to start message loop
 */
public class Peer {
    private static final Logger logger = LoggerFactory.getLogger(Peer.class);
    public static final int CONNECT_TIMEOUT_MSEC = 60000;

    private NetworkConnection connection;
    private final NetworkParameters params;


    private boolean running;
    private PeerAddress address;
    private final BlockChain blockChain;

    private Set<Inv> invKonwn = new HashSet<>();
    private Set<PeerAddress> addrKnown = new HashSet<>();

    public Peer(NetworkParameters params, PeerAddress address, BlockChain blockChain) {
        this.params = params;
        this.blockChain = blockChain;
        this.address = address;
    }

    /**
     * Construct a peer that uses the given, already connected network connection object.
     */
    public Peer(NetworkParameters params, BlockChain blockChain, NetworkConnection connection) {
        this(params, null, blockChain);
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
     * add a peer address into known addresses
     * @param addr
     */
    public void pushAddrKnown(PeerAddress addr) {
        addrKnown.add(addr);
    }

    /**
     * for test
     */
    public void setConnection(NetworkConnection connection) {
        this.connection = connection;
    }

    public void connect() throws PeerException {
        try {
            connection = new TCPNetworkConnection(address, params, CONNECT_TIMEOUT_MSEC);
        } catch (ProtocolException e) {
            throw new PeerException(e);
        } catch (IOException e) {
            throw new PeerException(e);
        }
    }
}
