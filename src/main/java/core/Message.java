package core;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Message that broadcast in the network
 */
public abstract class Message extends MessageHeader {
    private static final long serialVersionUID = 5188949531714732235L;
    private static final Logger logger = LoggerFactory.getLogger(Message.class);
}
