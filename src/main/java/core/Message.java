package core;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;

import static core.Utils.*;

/**
 * Message that broadcast in the network
 */
public class Message extends MessageHeader {
    private static final long serialVersionUID = 5188949531714732235L;
    private static final Logger logger = LoggerFactory.getLogger(Message.class);

    /** payload in a message */
    private byte[] payload;

    public Message() {
        super();
    }

    public Message(String command, int messageSize, byte[] payload) {
        super(command, messageSize);
        this.payload = payload;
        this.checksum = byteArrayToInt(doubleDigest(payload));
    }

    public void setPayload(byte[] payload) {
        this.payload = payload;
    }

    public byte[] getPayload() {
        return payload;
    }

    @Override
    public boolean valid() {
        if (!super.valid()) {
            return false;
        }

        int hashInt = byteArrayToInt(doubleDigest(payload));
        if (hashInt != checksum) {
            logger.error("Checksum incorrect!");
            return false;
        }
        return true;
    }
}
