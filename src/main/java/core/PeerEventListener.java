/**
 * Created By Yufan Wu
 * 2019/4/2
 */
package core;

import java.util.List;

/**
 * Invoke action when peer events happen
 */
public interface PeerEventListener {
    /**
     * when a blocked is recived
     *
     * @param peer  the peer receiving the block
     * @param block the block downloaded
     * @param left  the number of blocks to download
     */
    void onBlockDownloaded(Peer peer, Block block, int left);

    /**
     * when download start with the initial number of blokcs to be downloaded
     *
     * @param peer the peer receiving the block
     * @param left the number of blocks to download
     */
    void onChainDownloadStart(Peer peer, int left);

    /**
     * when a peer is connected
     *
     * @param peer      the peer
     * @param peerCount the total number of connected peers
     */
    void onPeerConnected(Peer peer, int peerCount);

    /**
     * when a peer is disconnected
     *
     * @param peer      the peer
     * @param peerCount the total number of connected peers
     */
    void onPeerDisconnected(Peer peer, int peerCount);

    /**
     * process "getdata" message
     *
     * @param peer           the peer
     * @param getDataMessage message whose type is "getdata"
     * @return a list of Message containing required information
     */
    List<Message> getData(Peer peer, Message getDataMessage);
}
