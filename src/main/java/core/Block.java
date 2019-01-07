/**
 * Created By Yufan Wu
 * 2018/12/24
 */
package core;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;

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

    // TODO
}
