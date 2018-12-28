/**
 * Created By Yufan Wu
 * 2018/12/23
 */
package net;

import core.Message;
import core.MessageHeader;
import exception.ProtocolException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;

import static core.Utils.*;

/**
 * {@code TCPNetworkConnection} is used for connecting a peer over the stand TCP/IP protocol
 */
public class TCPNetworkConnection implements NetworkConnection {
    private static final Logger logger = LoggerFactory.getLogger(TCPNetworkConnection.class);

    private final Socket socket;
    private final ObjectOutputStream out;
    private final ObjectInputStream in;
    private final PeerAddress peer;
    private final NetworkParameters params;

    /**
     * Connect to a given IP address and do the version handshake
     * | version -> |
     * | <- verback |
     * | <- version |
     * | verback -> |
     * @param peerAddress
     * @param params
     * @param connectTimeoutMsec
     * @throws IOException
     */
    public TCPNetworkConnection(PeerAddress peerAddress, NetworkParameters params, int connectTimeoutMsec) throws IOException, ProtocolException {
        this.params = params;
        this.peer = peerAddress;

        int port = (peerAddress.getPort() > 0) ? peerAddress.getPort() : params.port;
        InetSocketAddress address = new InetSocketAddress(peer.getAddr(), port);
        socket = new Socket();
        socket.connect(address, connectTimeoutMsec);

        out = new ObjectOutputStream(socket.getOutputStream());
        in = new ObjectInputStream(socket.getInputStream());

        //begin handshake
        logger.debug("Connecting and handshaking");
        writeMessage(createVerbackMessage());
        Message versionMsg = readMessage();
        if (!versionMsg.getCommand().equals(MessageHeader.VERSION)) {
            throw new ProtocolException("First message received was not a version message but rather " + versionMsg);
        }
        // TODO deal with version message
        writeMessage(createVerbackMessage());
        Message verbackMsg = readMessage();
        if (!verbackMsg.getCommand().equals(MessageHeader.VERBACK)) {
            throw new ProtocolException("Returned message was not a verback message but rather " + verbackMsg);
        }
        // handshake finished

        logger.info("Connect to peer: {}", peerAddress);
    }

    public TCPNetworkConnection(InetAddress address, NetworkParameters params, int connectTimeoutMsec) throws IOException, ProtocolException {
        this(new PeerAddress(address), params, connectTimeoutMsec);
    }

    /**
     * create a version message
     * @return version message
     * @throws IOException
     */
    private Message ceateVersionMessage() throws IOException {
        Message versionMessage = new Message(MessageHeader.VERSION, 0, null);
        byte[] payload = ObjectsToByteArray(); //TODO version message payload
        versionMessage.setMessageSize(payload.length);
        versionMessage.setPayload(payload);
        return versionMessage;
    }

    /**
     * create a verback message
     * @return
     */
    private Message createVerbackMessage() {
        return new Message(MessageHeader.VERBACK, 0, null);
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
}
