/**
 * Created By Yufan Wu
 * 2018/12/23
 */
package exception;

public class ProtocolException extends Exception {

    public ProtocolException(String msg) {
        super(msg);
    }

    public ProtocolException(Exception e) {
        super(e);
    }

    public ProtocolException(String msg, Exception e) {
        super(msg, e);
    }
}
