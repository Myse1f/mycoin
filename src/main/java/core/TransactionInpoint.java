/**
 * Created By Yufan Wu
 * 2019/2/25
 */
package core;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;

/**
 * Index of a transaction input
 */
public class TransactionInpoint implements Serializable {
    private static final long serialVersionUID = -2567908090986339975L;
    private static final Logger logger = LoggerFactory.getLogger(TransactionInpoint.class);

    private Transaction tx; // corresponding transaction
    private int n; // the index of the input in the transaction above

    public TransactionInpoint() {
        setNull();
    }

    public TransactionInpoint(Transaction tx, int n) {
        this.tx = tx;
        this.n = n;
    }

    public void setNull() {
        tx = null;
        n = -1;
    }

    public boolean isNull() {
        return (tx == null) && n == -1;
    }

    public Transaction getTx() {
        return tx;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;

        if (o == null || getClass() != o.getClass()) return false;

        TransactionInpoint that = (TransactionInpoint) o;

        return new EqualsBuilder()
                .append(n, that.n)
                .append(tx, that.tx)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
                .append(tx)
                .append(n)
                .toHashCode();
    }
}
