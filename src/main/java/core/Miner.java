/**
 * Created By Yufan Wu
 * 2019/4/16
 */
package core;

import exception.BlockPersistenceException;
import exception.ProtocolException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A miner is trying to solve the hash puzzle
 * It travel all the possible nonce until the PoW is reached or a new block tip is concatenated
 */
public class Miner {
    private static Logger logger = LoggerFactory.getLogger(Miner.class);

    private BlockChain blockChain;
    private PeerGroup peerGroup;
    private boolean working;

    private MinerThread miner;

    public Miner(BlockChain blockChain, PeerGroup peerGroup) {
        this.blockChain = blockChain;
        this.peerGroup = peerGroup;
        this.working = false;
    }

    /**
     * start solving puzzle, launch mining thread
     */
    public synchronized void run() {
        this.working = true;
        this.miner = new MinerThread();
        miner.start();
        logger.info("Start mining block");
    }

    /**
     * stop mining, interrupt the thread
     */
    public synchronized void stop() {
        this.working = false;
        this.miner = null; // finalize the miner
        logger.info("Stop mining block.");
    }

    public synchronized boolean isWorking() {
        return this.working;
    }

    private void handleBlockMined(Block block) {
        // todo
    }

    /**
     * create a block template for mining with nTime and nNonce unset
     */
    private Block createBlockTemplate(StoredBlock prevBlock) throws ProtocolException, BlockPersistenceException {
        return new Block(
                new BlockHead(prevBlock.getBlock().getHash(), -1, StoredBlock.getNextnBits(prevBlock, blockChain.getBlockPersistence()), 0)
        );
    }

    private class MinerThread extends Thread {

        public MinerThread() {
            super("Miner Thread");
            setPriority(Math.max(Thread.MIN_PRIORITY, Thread.currentThread().getPriority()-1));
            setDaemon(true);
        }

        @Override
        public void run() {
            try {
                while (isWorking()) {
                    Block template = createBlockTemplate(blockChain.getChainTip());
                }
            } catch (Exception e) {
                logger.error("Error in when mining, start shutdown miner.");
                Miner.this.stop();
            }
        }
    }
}
