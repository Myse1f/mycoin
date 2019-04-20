/**
 * Created By Yufan Wu
 * 2018/12/24
 */
package core;

import exception.VerificationException;
import net.NetworkParameters;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import utils.SpringContextUtil;

import java.io.IOException;
import java.math.BigInteger;
import java.util.Date;

import static core.Utils.doubleDigest;

/**
 * A {@link Block} contains a header which can validate its correctness
 * And it package a list of {@link Transaction}
 * The miner must solve the PoW to generate a valid block
 */
public class Block extends BlockHead {
    private static final long serialVersionUID = 3099818892119350169L;
    private static final Logger logger = LoggerFactory.getLogger(Block.class);
    /** maximum block size in bytes */
//    public static final int MAX_BLOCK_SIZE = 1000000;

    private transient SHA256Hash hash; // hash of the block, memory only
    //private transient List<SHA256Hash> merkleTree = new ArrayList<>(); // merkle tree of transactions

    //private List<Transaction> transactions = new ArrayList<>(); // transactions packaged into this block

    public Block() {
        setNull();
    }

    public Block(BlockHead header) {
        super(header);
    }

    public void setNull() {
        super.setNull();
//        transactions.clear();
//        merkleTree.clear();
    }

//    public SHA256Hash buildMerklrTree() {
//        merkleTree.clear();
//        for (Transaction t : transactions) {
//            merkleTree.add(t.getHash());
//        }
//        int levelOffset = 0; // Offset in the list where the currently processed level starts.
//        // Step through each level, stopping when we reach the root (levelSize == 1).
//        for (int levelSize = transactions.size(); levelSize > 1; levelSize = (levelSize + 1) / 2) {
//            // For each pair of nodes on that level:
//            for (int left = 0; left < levelSize; left += 2) {
//                // The right hand node can be the same as the left hand, in the case where we don't have enough transactions.
//                int right = Math.min(left + 1, levelSize - 1);
//                byte[] leftBytes = Utils.reverseBytes(merkleTree.get(levelOffset + left).getBytes());
//                byte[] rightBytes = Utils.reverseBytes(merkleTree.get(levelOffset + right).getBytes());
//                merkleTree.add(new SHA256Hash(Utils.reverseBytes(doubleDigestTwoBuffers(leftBytes, 0, 32, rightBytes, 0, 32))));
//            }
//            // Move to the next level.
//            levelOffset += levelSize;
//        }
//        return merkleTree.get(merkleTree.size()-1);
//    }

//    public SHA256Hash getTxHash(int index) {
//        return transactions.get(index).getHash();
//    }

    private static BigInteger LARGEST_HASH = BigInteger.ONE.shiftLeft(256);

    /**
     * Get the total work of the block
     *
     * Work is defined as the number of tries needed so a PoW problem in avrage.
     * It is represented by Reciprocal of probability.
     * e.g. 1/50 of the hash space to hit need 50 work.
     * @return work in BigInteger
     * @throws VerificationException
     */
    public BigInteger getWork() throws VerificationException {
        BigInteger target = getnBitsAsInteger();
        return LARGEST_HASH.divide(target.add(BigInteger.ONE));
    }

    public BigInteger getnBitsAsInteger() throws VerificationException {
        BigInteger target = Utils.decodeCompactBits(nBits);
        if (target.compareTo(BigInteger.valueOf(0)) <= 0 || target.compareTo(((NetworkParameters)(SpringContextUtil.getBean("network_params"))).proofOfWorkLimit) > 0) {
            throw new VerificationException("nBits is error: " + target.toString());
        }
        return target;
    }

    public boolean verifyBlock() throws VerificationException {
        BigInteger target = getnBitsAsInteger();
        BigInteger current = getHash().toBigInteger();
        if (current.compareTo(target) > 0) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        StringBuffer s = new StringBuffer(" block: \n"
                + "   hash: " + getHash().toString() + "\n"
                + "   previous block: " + hashPrevBlock.toString() + "\n"
                + "   time: [" + nTime + "] " + new Date(nTime * 1000).toString() + "\n"
                + "   difficulty target (nBits): 0x" + Long.toHexString(nBits) + "\n" + "   nonce: " + nNonce + "\n");

        return s.toString();
    }

    public SHA256Hash getHash() {
        if (hash == null) {
            try {
                hash = new SHA256Hash(Utils.reverseBytes(doubleDigest(Utils.objectsToByteArray((BlockHead)this))));
            } catch (IOException e) {
                throw new RuntimeException(e); // impossible
            }
        }
        return hash;
    }

    public void setHash(SHA256Hash hash) {
        this.hash = hash;
    }

//    public List<SHA256Hash> getMerkleTree() {
//        return merkleTree;
//    }
//
//    public List<Transaction> getTransactions() {
//        return transactions;
//    }
//
//    public void addTransaction(Transaction tx) {
//        transactions.add(tx);
//    }
}
