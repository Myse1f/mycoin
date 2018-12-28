/**
 * Created By Yufan Wu
 * 2018/12/28
 */
package core;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;

/**
 * A {@link TransactionInput} must be in a transaction
 * It is linked to a previous {@link TransactionOutput} which means consume the output
 */
public class TransactionInput implements Serializable {
    private static final long serialVersionUID = -7080009362836154695L;
    private static final Logger logger = LoggerFactory.getLogger(TransactionInput.class);

    /** index of previous transaction outpitut */
    TransactionOutpoint prevout;
    /** signature to unlock previous output */
    SHA256Hash signature;

    public TransactionInput(TransactionOutpoint prevout, SHA256Hash signature) {
        this.prevout = prevout;
        this.signature = signature;
    }

    public TransactionOutpoint getPrevout() {
        return prevout;
    }

    public void setPrevout(TransactionOutpoint prevout) {
        this.prevout = prevout;
    }

    public SHA256Hash getSignature() {
        return signature;
    }

    public void setSignature(SHA256Hash signature) {
        this.signature = signature;
    }
}
