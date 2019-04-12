/**
 * Created By Yufan Wu
 * 2018/12/23
 */
package core;

import org.junit.Test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;

public class MessageTest {

    @Test
    public void messageValidTest() {
        Message msg = new Message("tx", 1, new byte[]{0x10});
        assertTrue(msg.valid());
        msg.setChecksum(1);
        assertFalse(msg.valid());
    }



    @Test
    public void invMessageTest() throws IOException, ClassNotFoundException {
        Message msg = new Message("inv", 1, null);
        List<Inv> invs = new ArrayList<>();
        invs.add(new Inv(Inv.InvType.MSG_BLOCK, SHA256Hash.ZERO_HASH));
        invs.add(new Inv(Inv.InvType.MSG_BLOCK, SHA256Hash.ZERO_HASH));
        invs.add(new Inv(Inv.InvType.MSG_BLOCK, SHA256Hash.ZERO_HASH));
        invs.add(new Inv(Inv.InvType.MSG_BLOCK, SHA256Hash.ZERO_HASH));
        invs.add(new Inv(Inv.InvType.MSG_BLOCK, SHA256Hash.ZERO_HASH));
        msg.setInvsIntoPayload(invs);
        List<Inv> invs1 = msg.getPayloadAsInvs();
        assertEquals(invs, invs1);
    }


}
