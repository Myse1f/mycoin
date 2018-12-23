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
        assertTrue(mhdr.valid());
        mhdr.setMessageStart(new byte[]{0x78, 0x65, 0x43, 0x21});
        assertFalse(mhdr.valid());
        mhdr = new MessageHeader("tx\0a", 10);
        assertFalse(mhdr.valid());
        mhdr = new MessageHeader("tx\1", 10);
        assertFalse(mhdr.valid());
        mhdr = new MessageHeader("tx", -1);
        assertFalse(mhdr.valid());
        mhdr = new MessageHeader("tx", 0x02000001);
        assertFalse(mhdr.valid());
    }

    @Test
    public void MessgaeGetCommandTest() {
        MessageHeader mhdr = new MessageHeader("inv", 10);
        assertEquals(mhdr.getCommand(), "inv");
        mhdr = new MessageHeader("inv\0\0", 10);
        assertEquals(mhdr.getCommand(), "inv");
    }
}
