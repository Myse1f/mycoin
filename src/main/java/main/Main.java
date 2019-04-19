/**
 * Created By Yufan Wu
 * 2019/4/18
 */
package main;

import core.BlockChain;
import core.Miner;
import core.PeerGroup;
import exception.BlockPersistenceException;
import net.NetworkParameters;
import persistence.BlockPersistence;
import persistence.LevelDBBlockPersistence;

import java.io.File;
import java.io.IOException;

public class Main {
    public static void main(String[] args) throws BlockPersistenceException, IOException {
        NetworkParameters.setNetworkParameters(NetworkParameters.ID_TESTNET);
        BlockPersistence database = new LevelDBBlockPersistence(new File("data"));
        BlockChain chain  = new BlockChain(database);
        PeerGroup.init(chain);
        PeerGroup.getInstance().start();
        Miner miner = new Miner(chain, PeerGroup.getInstance());
    }
}
