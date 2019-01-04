/**
 * Created By Yufan Wu
 * 2018/12/28
 */
package core;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.Serializable;
import java.util.List;

/**
 * A {@link Transaction} contains a list of inputs and outputs like bitcoin
 * It can be packaged into a block which means that the transaction is valid
 */
public class Transaction implements Serializable {
    private static final long serialVersionUID = 8875805052125612642L;
    private static final Logger logger = LoggerFactory.getLogger(Transaction.class);

    List<TransactionInput> inputs;
    List<TransactionOutput> outputs;

    /** hash of the transaction */
    transient private SHA256Hash hash;
    //TODO
    public Transaction() {
        setNull();
    }

    public void setNull() {
        inputs.clear();
        outputs.clear();
    }

    public boolean isNull() {
        return (inputs.isEmpty() && outputs.isEmpty());
    }

    public boolean coinbase() {
        if (inputs.size() != outputs.size()) {
            return false;
        }
        if (inputs.get(0).getPrevout() != null) {
            return false;
        }
        for (int i=1; i<inputs.size(); i++) {
            if (inputs.get(i).getPrevout() == null) {
                return false;
            }
            // TODO verify input.value == output.value
        }

        return true;
    }

    public boolean valid() {
        return false;
    }

    public double calcInputValue() {
        return 0;
    }

    public SHA256Hash getHash() {
        if (hash != null) {
            return hash;
        }
        try {
            hash = new SHA256Hash(Utils.doubleDigest(Utils.ObjectsToByteArray(this)));
        } catch (IOException e) {
            logger.error(e.toString());
        }
        return hash;
    }

    public void addInput(TransactionInput in) {
        inputs.add(in);
    }

    public void addOutput(TransactionOutput out) {
        outputs.add(out);
    }
}
