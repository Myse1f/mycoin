/**
 * Created By Yufan Wu
 * 2018/12/23
 */
package net;

import core.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;
import java.math.BigInteger;

/**
 * NetworkParameters contains the data needed for working with network and chain
 * There are two kinds of network -- main net and test net
 */
public class NetworkParameters implements Serializable {
    private static final long serialVersionUID = -1117838171070972753L;
    private static final Logger logger = LoggerFactory.getLogger(NetworkParameters.class);

    /** id of main net */
    public static final String ID_MAINNET = "main";
    /** id of test net */
    public static final String ID_TESTNET = "test";

    /** Genesis block for the block chai */
    public Block genesisBlock;
    /** the proof of work difficulty */
    public BigInteger proofOfWorkLimit;
    /** Default TCP port on which to connect to nodes */
    public int port;
    /** difficulty adjustment periods in seconds */
    public int targetTimespan;
    /** difficulty adjustment periods in block number */
    public int interval;

    /** the id of network */
    private String id;

    /** set contructor private for singleton */
    public NetworkParameters(String id) {
        switch (id) {
            case ID_TESTNET:
                logger.info("Test Net.");
                createTestNet();
                break;
            case ID_MAINNET:
                logger.info("Main Net.");
                createMainNet();
                break;
            default:
                // impossible
                break;
        }
    }

    /**
     * create the genesis block according to network parameters
     * @return the genesis block
     */
    private static Block createGenesis() {
        return new Block();
    }

    private static final int TARGET_TIMESPAN = 24 * 60 * 60; // change difficulty every day
    private static final int TARGET_SPACING = 5 * 60; // 5 minutes per block
    private static final int BLOCK_INTERVAL = TARGET_TIMESPAN / TARGET_SPACING; // change difficulty every 288 blocks

    /**
     * setup the parameters of test net
     * @return the corresponding network parameters
     */
    private void createTestNet() {
        proofOfWorkLimit = new BigInteger("00000fffffffffffffffffffffffffffffffffffffffffffffffffffffffffff", 16);
        port = 23333;
        interval = BLOCK_INTERVAL;
        targetTimespan = TARGET_TIMESPAN;
        genesisBlock = createGenesis();
        genesisBlock.setnTime(1555744764L); // Sat Apr 20 15:19:24 CST 2019
        genesisBlock.setnBits(0x1e0fffff);
        genesisBlock.setnNonce(427685L); // dont care about nonce of genesis block
        id = ID_TESTNET;
    }

    /**
     * setup the parameters of main net
     * @return the corresponding network parameters
     */
    private void createMainNet() {
        // TODO
    }

    public String getId() {
        return id;
    }
}
