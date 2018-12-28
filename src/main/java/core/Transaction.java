/**
 * Created By Yufan Wu
 * 2018/12/28
 */
package core;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

    transient SHA256Hash hash;
    //TODO

    public boolean coinbase() {
        return false;
    }

    public boolean valid() {
        return false;
    }

    public double calcInputValue() {
        return 0;
    }

}
