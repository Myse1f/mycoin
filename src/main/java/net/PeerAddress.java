/**
 * Created By Yufan Wu
 * 2018/12/23
 */
package net;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;
import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 * A PeerAddress holds an IP address and its port number of a peer, representing the network location
 */
public class PeerAddress implements Serializable {
    private static final long serialVersionUID = -1398227674652250300L;
    private static final Logger logger = LoggerFactory.getLogger(PeerAddress.class);

    /** IP */
    private InetAddress addr;
    /** port */
    private int port;
    /** time */
    private long time;

    public PeerAddress(InetAddress addr, int port) {
        this.addr = addr;
        this.port = port;
    }

    public PeerAddress(byte[] addr, int port) {
        try {
            this.addr = InetAddress.getByAddress(addr);
            this.port = port;
        } catch (UnknownHostException e) {
            // impossible
            throw new RuntimeException(e);
        }
    }

    public InetAddress getAddr() {
        return addr;
    }

    public int getPort() {
        return port;
    }


    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }

    @Override
    public String toString() {
        return "[" + addr.getHostAddress() + "]:" + port;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof PeerAddress)) return false;
        PeerAddress other = (PeerAddress) o;
        return other.addr.equals(addr) &&
                other.port == port &&
                other.time == time;
    }

    @Override
    public int hashCode() {
        return addr.hashCode() ^ port ^ (int) time;
    }
}
