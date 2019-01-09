/**
 * Created By Yufan Wu
 * 2019/1/9
 */
package core;

import org.junit.Test;

import java.io.IOException;

import static org.junit.Assert.*;

public class TransactionOutputTest {
    private TransactionOutput out = new TransactionOutput();

    @Test
    public void serializationTest() {
        try {
            assertEquals(Utils.objectsToByteArray(out).length, Utils.getObjectSerializedSize(out));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void nullTest() {
        assertTrue(out.isNull());
        out.setValue(1);
        assertFalse(out.isNull());
    }
}
