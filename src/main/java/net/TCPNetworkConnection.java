/**
 * Created By Yufan Wu
 * 2018/12/23
 */
package net;

import core.Message;
import exception.ProtocolException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;

/**
 * {@link TCPNetworkConnection} is used for connecting a peer over the stand TCP/IP protocol
 */
public class TCPNetworkConnection implements NetworkConnection {
    private static final Logger logger = LoggerFactory.getLogger(TCPNetworkConnection.class);

    private final Socket socket;
    private final ObjectOutputStream out;
    private final ObjectInputStream in;
    private final PeerAddress peer;

    /**
     * Create a connection with connected socket
     */
    public TCPNetworkConnection(Socket socket) throws IOException {
        this.socket = socket;
        this.peer = new PeerAddress(socket.getInetAddress(), socket.getPort());
        in = new ObjectInputStream(socket.getInputStream());
        out = new ObjectOutputStream(socket.getOutputStream());
    }

    /**
     * Connect to a given IP address and do the version handshake
     * | version -> |
     * | <- version |
     * | <- verback |
     * | verback -> |
     * 
     * @param peerAddress
     * @param connectTimeoutMsec
     * @throws IOException
     */
    public TCPNetworkConnection(PeerAddress peerAddress, int connectTimeoutMsec, Message versionMessage, NetworkParameters params)
            throws IOException {
        this.peer = peerAddress;
        int port = (peerAddress.getPort() > 0) ? peerAddress.getPort() : params.port;
        InetSocketAddress address = new InetSocketAddress(peer.getAddr(), port);
        socket = new Socket();
        socket.connect(address, connectTimeoutMsec);

        out = new ObjectOutputStream(socket.getOutputStream());
        in = new ObjectInputStream(socket.getInputStream());

        //begin handshake
        logger.debug("Connecting and handshaking");
        writeMessage(versionMessage);
//        Message versionMsg = readMessage();
//        if (!versionMsg.getCommand().equals(MessageHeader.VERSION)) {
//            throw new ProtocolException("First message received was not a version message but rather " + versionMsg);
//        }
//
//        writeMessage(createVerbackMessage());
//        Message verbackMsg = readMessage();
//        if (!verbackMsg.getCommand().equals(MessageHeader.VERBACK)) {
//            throw new ProtocolException("Returned message was not a verback message but rather " + verbackMsg);
//        }
//        // handshake finished

//        logger.info("Connect to peer: {}", peerAddress);
    }

    public TCPNetworkConnection(InetAddress address, int connectTimeoutMsec, Message versionMessage, NetworkParameters params) throws IOException {
        this(new PeerAddress(address), connectTimeoutMsec, versionMessage, params);
    }

    @Override
    public void shutdown() throws IOException {
        out.close();
        in.close();
        socket.shutdownOutput();
        socket.shutdownInput();
        socket.close();
    }

    /**
     * Read a message from socket input stream
     * @return
     * @throws IOException
     * @throws ProtocolException
     */
    @Override
    public Message readMessage() throws IOException, ProtocolException {
        Object obj;
        try {
            obj = in.readObject();
        } catch (ClassNotFoundException e) {
            throw new ProtocolException(e);
        }
        if (obj instanceof Message) {
            return (Message)obj;
        } else {
            logger.error("Read a non-message!");
            throw new ProtocolException("non-message!");
        }
    }

    /**
     * write message into socket thread-safely
     * @param message
     * @throws IOException
     */
    @Override
    public void writeMessage(Message message) throws IOException {
        synchronized (out) {
            out.writeObject(message);
        }

    }

    @Override
    public PeerAddress getPeerAddress() {
        return peer;
    }

    @Override
    public String toString() {
        return "[" + peer.getAddr().getHostAddress() + "]:" + peer.getPort() + " (" + (socket.isConnected() ? "connected" :
                "disconnected") + ")";
    }
}
