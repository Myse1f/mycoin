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
    private static final long serialVersionUID = -7630821239286970631L;
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

    private static NetworkParameters n = null;

    /** set contructor private for singleton */
    private NetworkParameters() {}

    /**
     * create the genesis block according to network parameters
     * @return the genesis block
     */
    private static Block createGenesis() {
        /**
         * genesis block contain only a coinbase transaction to none
         */
        Block genesisBlock = new Block();
//        Transaction tx = new Transaction();
//        tx.addInput(new TransactionInput());
//        tx.addOutput(new TransactionOutput(25.0, SHA256Hash.ZERO_HASH));
//        genesisBlock.addTransaction(tx);
        return genesisBlock;
    }

    private static final int TARGET_TIMESPAN = 24 * 60 * 60; // change difficulty every day
    private static final int TARGET_SPACING = 5 * 60; // 5 minutes per block
    private static final int BLOCK_INTERVAL = TARGET_TIMESPAN / TARGET_SPACING; // change difficulty every 288 blocks

    /**
     * setup the parameters of test net
     * @return the corresponding network parameters
     */
    private static void createTestNet() {
        n = new NetworkParameters();
        n.proofOfWorkLimit = new BigInteger("0000000fffffffffffffffffffffffffffffffffffffffffffffffffffffffff", 16);
        n.port = 23333;
        n.interval = BLOCK_INTERVAL;
        n.targetTimespan = TARGET_TIMESPAN;
        n.genesisBlock = createGenesis();
        n.genesisBlock.setnTime(1555554791L); // Thu Apr 18 10:33:11 CST 2019
        n.genesisBlock.setnBits(0x1d0fffff);
        n.genesisBlock.setnNonce(112590263L); // dont care about nonce of genesis block
        n.id = ID_TESTNET;
    }

    /**
     * setup the parameters of main net
     * @return the corresponding network parameters
     */
    private static void createMainNet() {
        // TODO
    }

    /**
     * set network environment: test or main
     * must be run at the beginning of program only once
     */
    public static void setNetworkParameters(String id) {
        switch (id) {
            case ID_TESTNET:
                logger.info("Test Net.");
                createTestNet();
                break;
            case ID_MAINNET:
                createMainNet();
                break;
            default:
                // impossible
                break;
        }
    }

    public static NetworkParameters getNetworkParameters() {
        if (n == null) {
            logger.error("setNetworkParameters must be run before!");
            throw new RuntimeException("unset network!");
        }
        return n;
    }

    public String getId() {
        return id;
    }
}
