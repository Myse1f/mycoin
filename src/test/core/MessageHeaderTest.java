/**
 * Created By Yufan Wu
 * 2018/12/22
 */
package core;


import org.junit.Test;

import static org.junit.Assert.*;

public class MessageHeaderTest {

    @Test
    public void MessageValidTest() {
        MessageHeader mhdr = new MessageHeader("tx", 10);
        assertTrue(mhdr.isValid());
        mhdr = new MessageHeader("tx\0a", 10);
        assertFalse(mhdr.isValid());
        mhdr = new MessageHeader("tx\1", 10);
        assertFalse(mhdr.isValid());
        mhdr = new MessageHeader("tx", -1);
        assertFalse(mhdr.isValid());
        mhdr = new MessageHeader("tx", 0x02000001);
        assertFalse(mhdr.isValid());
    }

    @Test
    public void MessgaeGetCommandTest() {
        MessageHeader mhdr = new MessageHeader("inv", 10);
        assertEquals(mhdr.getCommand(), "inv");
        mhdr = new MessageHeader("inv\0\0", 10);
        assertEquals(mhdr.getCommand(), "inv");
    }
}
