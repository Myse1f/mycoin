/**
 * Created By Yufan Wu
 * 2019/3/25
 */
package core;

import org.junit.Test;

import java.math.BigInteger;

import static org.junit.Assert.*;

public class UtilsTest {

    @Test
    public void getSetCompactTest() {
        assertEquals(Utils.decodeCompactBits(Utils.encodeCompactBits(new BigInteger("123456789", 10))), new BigInteger("123456789", 10));
    }
}
