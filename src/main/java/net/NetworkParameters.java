/**
 * Created By Yufan Wu
 * 2018/12/23
 */
package net;

import java.io.Serializable;

/**
 * NetworkParameters contains the data needed for working with network and chain
 * There are two kinds of network -- main net and test net
 */
public class NetworkParameters implements Serializable {
    private static final long serialVersionUID = -7630821239286970631L;

    public static final String ID_MAINNET = "main";
    public static final String ID_TESTNET = "test";
}
