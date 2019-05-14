/**
 * Created By Yufan Wu
 * 2019/4/18
 */
package core;

import exception.VerificationException;
import net.NetworkParameters;
import org.junit.Test;

import java.io.IOException;
import java.math.BigInteger;

import static org.junit.Assert.assertTrue;


public class BlockTest {
    @Test
    public void getGenesisBlock() throws VerificationException, IOException {
        long now = System.currentTimeMillis() / 1000;
        NetworkParameters params = new NetworkParameters(NetworkParameters.ID_TESTNET);
        Block genesis = new Block(new BlockHead(SHA256Hash.ZERO_HASH, now, 0x1e00dfff, 0));
        int no = 0;
        BigInteger target = genesis.getnBitsAsInteger();
        while (true) {
            SHA256Hash hash = new SHA256Hash(Utils.reverseBytes(Utils.doubleDigest(Utils.objectsToByteArray((BlockHead)genesis))));
            BigInteger current = hash.toBigInteger();
            if (current.compareTo(target) <= 0) {
                // available nNonce is found
                break;
            }
            long nonce = genesis.getnNonce();
            if (nonce + 1 > 0xFFFFFFFFL) {
                // nNonce is overflow
                System.out.println("Nonce overflow");
                break;
            }
            if (nonce % 1000 == 0) {
                System.out.println("Epoch :" + no++ + " Hash:" + hash.toString());
                System.out.println("Target: " + target.toString(16));
                System.out.println("Current: " + current.toString(16));
            }
            genesis.setnNonce(nonce + 1);
        }
        System.out.println(genesis);
        System.out.println(now);
        System.out.println(Long.toHexString(Utils.encodeCompactBits((params.proofOfWorkLimit))));
        System.out.println(Long.toHexString(Utils.encodeCompactBits(Utils.decodeCompactBits(params.genesisBlock.nBits))));
    }

    @Test
    public void genesisBlockTest() throws VerificationException {
        NetworkParameters params = new NetworkParameters(NetworkParameters.ID_TESTNET);
        assertTrue(params.genesisBlock.verifyBlock());
    }
}
