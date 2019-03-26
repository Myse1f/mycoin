/**
 * Created By Yufan Wu
 * 2019/3/26
 */
package core;


import net.NetworkParameters;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;

public class BlockHeadTest {
    @Before
    public void beforeTest() {
        NetworkParameters.setNetworkParameters(NetworkParameters.ID_TESTNET);
    }

    @Test
    public void blockHeadSizeTest() throws IOException {
        System.out.println(Utils.objectsToByteArray(NetworkParameters.getNetworkParameters().genesisBlock).length);
    }
}
