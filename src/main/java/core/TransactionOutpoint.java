/**
 * Created By Yufan Wu
 * 2018/12/28
 */
package core;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;

/**
 * A {@link TransactionOutpoint} index a corresponding transaction output
 * by a transaction hash and the position of the output in that transaction
 */
public class TransactionOutpoint implements Serializable {
    private static final long serialVersionUID = 2522241225027684239L;
    private static final Logger logger = LoggerFactory.getLogger(TransactionOutpoint.class);

    /** transaction hash */
    private SHA256Hash hash;
    /** the index of output */
    private int n;

    public TransactionOutpoint() {
        setNull();
    }

    public TransactionOutpoint(SHA256Hash hash, int n) {
        this.hash = hash;
        this.n = n;
    }

    public void setNull() {
        hash = SHA256Hash.ZERO_HASH;
        n = -1;
    }

    public boolean isNull() {
        return (hash.equals(SHA256Hash.ZERO_HASH) && n == -1);
    }

    public SHA256Hash getHash() {
        return hash;
    }

    public void setHash(SHA256Hash hash) {
        this.hash = hash;
    }

    public int getN() {
        return n;
    }

    public void setN(int n) {
        this.n = n;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;

        if (o == null || getClass() != o.getClass()) return false;

        TransactionOutpoint that = (TransactionOutpoint) o;

        return new EqualsBuilder()
                .append(n, that.n)
                .append(hash, that.hash)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
                .append(hash)
                .append(n)
                .toHashCode();
    }
}
