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
import java.net.Socket;

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

    @Override
    public void shutdown() throws IOException {
        out.close();
        in.close();
        socket.shutdownOutput();
        socket.shutdownInput();
        socket.close();
    }

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
