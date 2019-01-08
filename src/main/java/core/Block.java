/**
 * Created By Yufan Wu
 * 2018/12/24
 */
package core;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

import static core.Utils.doubleDigestTwoBuffers;

/**
 * A {@link Block} contains a header which can validate its correctness
 * And it package a list of {@link Transaction}
 * The miner must solve the PoW to generate a valid block
 */
public class Block extends BlockHeader {
    private static final long serialVersionUID = 3099818892119350169L;
    private static final Logger logger = LoggerFactory.getLogger(Block.class);
    /** maximum block size in bytes */
    public static final int MAX_BLOCK_SIZE = 1000000;

    private transient SHA256Hash hash; // hash of the block, memory only
    private transient List<SHA256Hash> merkleTree = new ArrayList<>(); // merkle tree of transactions

    private List<Transaction> transactions; // transactions packaged into this block

    public Block() {
        setNull();
    }

    public Block(BlockHeader header) {
        super(header);
    }

    public void setNull() {
        super.setNull();
        transactions.clear();
        merkleTree.clear();
    }

    public SHA256Hash buildMerklrTree() {
        merkleTree.clear();
        for (Transaction t : transactions) {
            merkleTree.add(t.getHash());
        }
        int levelOffset = 0; // Offset in the list where the currently processed level starts.
        // Step through each level, stopping when we reach the root (levelSize == 1).
        for (int levelSize = transactions.size(); levelSize > 1; levelSize = (levelSize + 1) / 2) {
            // For each pair of nodes on that level:
            for (int left = 0; left < levelSize; left += 2) {
                // The right hand node can be the same as the left hand, in the case where we don't have enough transactions.
                int right = Math.min(left + 1, levelSize - 1);
                byte[] leftBytes = Utils.reverseBytes(merkleTree.get(levelOffset + left).getBytes());
                byte[] rightBytes = Utils.reverseBytes(merkleTree.get(levelOffset + right).getBytes());
                merkleTree.add(new SHA256Hash(Utils.reverseBytes(doubleDigestTwoBuffers(leftBytes, 0, 32, rightBytes, 0, 32))));
            }
            // Move to the next level.
            levelOffset += levelSize;
        }
        return merkleTree.get(merkleTree.size()-1);
    }

    public SHA256Hash getTxHash(int index) {
        return transactions.get(index).getHash();
    }

    @Override
    public String toString() {
        return super.toString();
    }

    // TODO
}
