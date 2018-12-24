/**
 * Created By Yufan Wu
 * 2018/12/23
 */
package net;

import core.Block;

import java.io.Serializable;
import java.math.BigInteger;

/**
 * NetworkParameters contains the data needed for working with network and chain
 * There are two kinds of network -- main net and test net
 */
public class NetworkParameters implements Serializable {
    private static final long serialVersionUID = -7630821239286970631L;

    /** id of main net */
    public static final String ID_MAINNET = "main";
    /** id of test net */
    public static final String ID_TESTNET = "test";

    /** Genesis block for the block chai */
    public Block genesisBlock;
    /** the proof of work difficulty */
    public BigInteger proofOfWork;
    /** Default TCP port on which to connect to nodes */
    public int port;

    /** the id of network */
    private String id;

    /**
     * create the genesis block according to network parameters
     * @param n network parameters test or main
     * @return the genesis block
     */
    private static Block createGenesis(NetworkParameters n) {
        //TODO create a genesis block according to network parameters
        return null;
    }

    /**
     * setup the parameters of test net
     * @param n
     * @return the corresponding network parameters
     */
    private static NetworkParameters createTestNet(NetworkParameters n) {
        // TODO
        return n;
    }

    public static NetworkParameters testNet() {
        NetworkParameters n = new NetworkParameters();
        return createTestNet(n);
    }

    /**
     * setup the parameters of main net
     * @param n
     * @return the corresponding network parameters
     */
    private static NetworkParameters createMainNet(NetworkParameters n) {
        // TODO
        return n;
    }

    public static NetworkParameters mainNet() {
        NetworkParameters n = new NetworkParameters();
        return createMainNet(n);
    }

    public String getId() {
        return id;
    }
}
