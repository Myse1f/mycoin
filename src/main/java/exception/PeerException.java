/**
 * Created By Yufan Wu
 * 2018/12/26
 */
package exception;

public class PeerException extends Exception {
    public PeerException(String msg) {
        super(msg);
    }

    public PeerException(Exception e) {
        super(e);
    }

    public PeerException(String msg, Exception e) {
        super(msg, e);
    }
}
