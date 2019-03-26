/**
 * Created By Yufan Wu
 * 2019/3/26
 */
package exception;

public class BlockPersistenceException extends Exception {
    public BlockPersistenceException(String message) {
        super(message);
    }

    public BlockPersistenceException(Throwable t) {
        super(t);
    }
}
