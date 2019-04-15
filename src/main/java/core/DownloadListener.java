/**
 * Created By Yufan Wu
 * 2019/4/15
 */
package core;

import java.text.DateFormat;
import java.util.Date;

/**
 * listener for listening block download events
 */
public class DownloadListener extends AbstractPeerEventListener {
    private int originalBlocksLeft = -1;
    private int lastPercent = 0;

    @Override
    public void onChainDownloadStart(Peer peer, int blocksLeft) {
        startDownload(blocksLeft);
        originalBlocksLeft = blocksLeft;
        if (blocksLeft == 0) {
            doneDownload();
        }
    }

    @Override
    public void onBlockDownloaded(Peer peer, Block block, int blocksLeft) {
        if (blocksLeft == 0) {
            doneDownload();
        }

        if (blocksLeft < 0 || originalBlocksLeft <= 0)
            return;

        double pct = 100.0 - (100.0 * (blocksLeft / (double) originalBlocksLeft));
        if ((int) pct != lastPercent) {
            progress(pct, blocksLeft, new Date(block.getnTime() * 1000));
            lastPercent = (int) pct;
        }
    }

    /**
     * Called when download progress is made.
     *
     * @param pct  the percentage of chain downloaded, estimated
     * @param date the date of the last block downloaded
     */
    private void progress(double pct, int blocksSoFar, Date date) {
        System.out.println(String.format("Chain download %d%% done with %d blocks to go, block date %s", (int) pct,
                blocksSoFar, DateFormat.getDateTimeInstance().format(date)));
    }

    /**
     * Called when download is initiated.
     *
     * @param blocks the number of blocks to download, estimated
     */
    private void startDownload(int blocks) {
        System.out.println("Downloading block chain of size " + blocks + ". " +
                (blocks > 1000 ? "This may take a while." : ""));
    }

    /**
     * Called when we are done downloading the block chain.
     */
    private void doneDownload() {
        System.out.println("Done downloading block chain");
    }
}
