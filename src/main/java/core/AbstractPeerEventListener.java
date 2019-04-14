/**
 * Created By Yufan Wu
 * 2019/4/2
 */
package core;

import java.util.List;

/**
 * Abstract Listener implements {@link PeerEventListener}
 * The default method implementation do nothing.
 * For easily implement specific method
 */
public class AbstractPeerEventListener implements PeerEventListener {

    @Override
    public void onBlockDownload(Peer peer, Block block, int left) {

    }

    @Override
    public void onChainDownloadStart(Peer peer, int left) {

    }

    @Override
    public void onPeerConnected(Peer peer, int peerCount) {

    }

    @Override
    public void onPeerDisconnected(Peer peer, int peerCount) {

    }

    @Override
    public List<Message> getData(Peer peer, Message getDataMessage) {
        return null;
    }
}
