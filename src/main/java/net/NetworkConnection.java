/**
 * Created By Yufan Wu
 * 2018/12/23
 */
package net;

import core.Message;
import exception.ProtocolException;

import java.io.IOException;

/**
 * NetworkConnection is an interface to support multiple low level protocols
 * It handles talking to a remote peers at a low level, such as how to read and write messages
 */
public interface NetworkConnection {
    /**
     * Shuts down the network socket
     * @throws IOException
     */
    void shutdown() throws IOException;

    /**
     * Reads a network message, blocking until the message is fully received
     * @return
     * @throws IOException
     */
    Message readMessage() throws IOException, ProtocolException;

    /**
     * Writes the given message out over the network
     * @param message
     * @throws IOException
     */
    void writeMessage(Message message) throws IOException;

    /**
     * Returns the address of the other side of the network connection
     */
    public PeerAddress getPeerAddress();
}
