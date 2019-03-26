/**
 * Created By Yufan Wu
 * 2018/12/28
 */
package core;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * A {@link Transaction} contains a list of inputs and outputs like bitcoin
 * It can be packaged into a block which means that the transaction is valid
 */
public class Transaction implements Serializable {
    private static final long serialVersionUID = 8875805052125612642L;
    private static final Logger logger = LoggerFactory.getLogger(Transaction.class);

    private List<TransactionInput> inputs;
    private List<TransactionOutput> outputs;

    /** hash of the transaction */
    transient private SHA256Hash hash;
    //TODO
    public Transaction() {
        inputs = new ArrayList<>();
        outputs = new ArrayList<>();
        setNull();
    }

    public void setNull() {
        inputs.clear();
        outputs.clear();
    }

    public boolean isNull() {
        return (inputs.isEmpty() && outputs.isEmpty());
    }

    public boolean isCoinbase() {
        if (inputs.size() != outputs.size()) {
            return false;
        }
        if (!inputs.get(0).getPrevout().isNull()) {
            return false;
        }
        return true;
    }

    // TODO context-free validate
    public boolean isValid() {
        // inputs and outputs can't be empty
        if (inputs.isEmpty()) {
            logger.info("isValid(); inputs empty");
            return false;
        }
        if (outputs.isEmpty()) {
            logger.info("isValid(): outputs empty");
            return false;
        }
        // check for negative output
        for (TransactionOutput out : outputs) {
            if (out.getValue() < 0) {
                logger.info("isValid(): output value is negative");
                return false;
            }
        }
        // check for duplicated inputs
        Set<TransactionOutpoint> setInOutpoint = new HashSet<>();
        for (TransactionInput in : inputs) {
            if (setInOutpoint.contains(in.getPrevout())) {
                logger.info("isValid(): duplicated input");
                return false;
            }
            setInOutpoint.add(in.getPrevout());
        }
        // check prevout
        if (isCoinbase()) {
            // TODO verify input.value == output.value
        } else {
            for (TransactionInput in : inputs) {
                if (in.getPrevout().isNull()) {
                    logger.info("isValid(): null previous output");
                    return false;
                }
            }
        }
        return true;
    }

    // TODO pass a Coins db
    public double getInputValue() {
        return 0;
    }

    public double getOutputValue() {
        double outVal = 0;
        for (TransactionOutput out : outputs) {
            outVal += out.getValue();
        }
        return outVal;
    }

    public SHA256Hash getHash() {
        if (hash != null) {
            return hash;
        }
        try {
            hash = new SHA256Hash(Utils.doubleDigest(Utils.objectsToByteArray(this)));
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

    public List<TransactionInput> getInputs() {
        return inputs;
    }

    public List<TransactionOutput> getOutputs() {
        return outputs;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;

        if (o == null || getClass() != o.getClass()) return false;

        Transaction that = (Transaction) o;

        return new EqualsBuilder()
                .append(inputs, that.inputs)
                .append(outputs, that.outputs)
                .append(hash, that.hash)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
                .append(inputs)
                .append(outputs)
                .append(hash)
                .toHashCode();
    }
}
