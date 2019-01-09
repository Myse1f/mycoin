/**
 * Created By Yufan Wu
 * 2019/1/9
 */
package core;

import org.junit.Test;

import java.io.IOException;

import static org.junit.Assert.assertEquals;

public class TransactionInputTest {
    TransactionInput in = new TransactionInput();

    @Test
    public void serializationTest() {
        try {
            assertEquals(Utils.objectsToByteArray(in).length, Utils.getObjectSerializedSize(in));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
