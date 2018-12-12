package core;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;

/**
 * Message that broadcast in the network
 */
public abstract class Message implements Serializable {
    private static final long serialVersionUID = 5188949531714732235L;
}
