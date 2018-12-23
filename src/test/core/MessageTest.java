/**
 * Created By Yufan Wu
 * 2018/12/23
 */
package core;

import org.junit.Test;

import static org.junit.Assert.*;

public class MessageTest {

    @Test
    public void messageValidTest() {
        Message msg = new Message("tx", 1, new byte[]{0x10});
        assertTrue(msg.valid());
        msg.setChecksum(1);
        assertFalse(msg.valid());
    }
}
