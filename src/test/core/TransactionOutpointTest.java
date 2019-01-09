/**
 * Created By Yufan Wu
 * 2019/1/9
 */
package core;

import org.junit.Test;

import java.io.IOException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class TransactionOutpointTest {
    private TransactionOutpoint outpoint = new TransactionOutpoint();

    @Test
    public void serializationTest() {
        try {
            assertEquals(Utils.objectsToByteArray(outpoint).length, Utils.getObjectSerializedSize(outpoint));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void nullTest() {
        assertTrue(outpoint.isNull());
        outpoint.setN(1);
        assertFalse(outpoint.isNull());
    }
}
