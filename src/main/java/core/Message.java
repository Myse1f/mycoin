package core;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

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
        if (payload != null) {
            this.checksum = byteArrayToInt(doubleDigest(payload));
        }
    }

    public void setPayload(byte[] payload) {
        this.payload = payload;
        this.checksum = byteArrayToInt(doubleDigest(payload));
    }

    public byte[] getPayload() {
        return payload;
    }

    /**
     * get a list of inv from the payload
     * @return List of inv
     * @throws IOException
     * @throws ClassNotFoundException
     */
    public List<Inv> getPayloadAsInvs() throws IOException, ClassNotFoundException {
        List<Inv> invs = new ArrayList<>();
        ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(payload));
        while (true) {
            try {
                Inv inv = Inv.readInv(ois);
                invs.add(inv);
            } catch (EOFException e) {
                // the end of the stream
                break;
            }
        }
        ois.close();
        return invs;
    }

    /**
     * get a Integer which is the version height from the payload
     * @return an integer
     */
    public int getPayloadAsInteger() throws IOException, ClassNotFoundException {
        ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(payload));
        int height = (Integer) ois.readObject();
        ois.close();
        return height;
    }

    /**
     * get a block from the payload
     * @return the block
     */
    public Block getPayloadAsBlock() throws IOException, ClassNotFoundException {
        ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(payload));
        Block block = (Block) ois.readObject();
        ois.close();
        return block;
    }

    /**
     * get blocks locator(a list) and a hash from the payload and return a Object array
     * which the first one is locator and the second is hashStop
     */
    public Object[] getPayloadAsLocatorAndHash() throws IOException, ClassNotFoundException {
        ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(payload));
        SHA256Hash[] locator= (SHA256Hash[]) ois.readObject();
        //List<SHA256Hash> locator = Arrays.asList(locatorArray);
        SHA256Hash hashStop = (SHA256Hash) ois.readObject();
        ois.close();
        return new Object[]{locator, hashStop};
    }

    /**
     * set a list of inv into the message payload
     */
    public void setInvsIntoPayload(List<Inv> invs) throws IOException {
        this.payload = Utils.objectsToByteArray(invs);
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
