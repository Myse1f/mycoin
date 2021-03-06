/**
 * Created By Yufan Wu
 * 2018/12/22
 */
package core;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;
import java.util.Arrays;

/**
 *  messgae start (4 bytes)
 *  command (12 bytes)
 *  size (4 bytes)
 *  checksum (4 bytes)
 */
public class MessageHeader implements Serializable {

    private static final long serialVersionUID = 480990842310659879L;
    private static final Logger logger = LoggerFactory.getLogger(Message.class);

    public static final byte[] MESSAGE_START = new byte[]{0x12, 0x34, 0x56, 0x78};
    public static final int MESSAGE_START_SIZE = MESSAGE_START.length;
    public static final int COMMAND_SIZE = 12;
    public static final int MESSAGE_SIZE_SIZE = Integer.BYTES;
    public static final int CHECKSUM_SIZE = Integer.BYTES;
    public static final int SIZE = MESSAGE_START_SIZE + COMMAND_SIZE + MESSAGE_SIZE_SIZE + CHECKSUM_SIZE;
    public static final int MAX_MESSAGE_SIZE = 0x02000000;

    /** message type */
    public static final String VERSION = "version";
    public static final String VERBACK = "verback";
    public static final String ADDR = "addr";
    public static final String INV = "inv";
    public static final String GETDATA = "gettdata";
    public static final String GETBLOCKS = "getblocks";
    public static final String TX = "tx";
    public static final String BLOCK = "block";

    /** header info */
    protected byte[] messageStart;
    protected byte[] command;
    protected int messageSize;
    protected int checksum;

    public MessageHeader() {
        this.messageStart = Arrays.copyOf(MESSAGE_START, MESSAGE_START_SIZE);
        this.command = null;
        this.messageSize = -1;
        this.checksum = 0;
    }

    public MessageHeader(String command, int messageSize) {
        this.messageStart = Arrays.copyOf(MESSAGE_START, MESSAGE_START_SIZE);
        this.command = Arrays.copyOf(command.getBytes(), COMMAND_SIZE);
        this.messageSize = messageSize;
        this.checksum = 0;
    }

    /** for test */
    public void setChecksum(int checksum) {
        this.checksum = checksum;
    }

    /** for test */
    public void setMessageStart(byte[] messageStart) {
        this.messageStart = messageStart;
    }

    public String getCommand() {
        return new String(command).replaceAll("\0", ""); // delete unused 0 in the end
    }

    public boolean valid() {
        /** check start bytes */
        if (!Arrays.equals(messageStart, MESSAGE_START)) {
            logger.error("Message start unmatched!");
            return false;
        }

        /** check command */
        for (int i=0; i<COMMAND_SIZE; i++) {
            if (command[i] == 0) {
                // must be all zeros after the first zero
                for ( ; i<COMMAND_SIZE; i++) {
                    if (command[i] != 0) {
                        logger.error("Command has non-consecutive zero suffix!");
                        return false;
                    }
                }
            } else if (command[i] < ' ' || command[i] > 0x7E) {
                logger.error("Command contains invisible characters!");
                return false;
            }
        }

        return true;
    }

    @Override
    public String toString() {
        StringBuilder ret = new StringBuilder();
        ret.append("[Commamd: ");
        ret.append(new String(command));
        ret.append(' ');
        ret.append("MessageSize: ");
        ret.append(messageSize);
        ret.append(']');
        return ret.toString();
    }

    public void setMessageSize(int messageSize) {
        this.messageSize = messageSize;
    }
}
