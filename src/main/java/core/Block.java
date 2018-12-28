/**
 * Created By Yufan Wu
 * 2018/12/24
 */
package core;

import java.io.Serializable;

/**
 * A {@link Block} contains a header which can validate its correctness
 * And it package a list of {@link Transaction}
 * The miner must solve the PoW to generate a valid block
 */
public class Block implements Serializable {
    private static final long serialVersionUID = 3099818892119350169L;
    // TODO
}
