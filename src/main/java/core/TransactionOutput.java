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
 * A {@link TransactionOutput} must be in a {@link Transaction}
 * An output locks some coins and only the receiver can unlock it
 */
public class TransactionOutput implements Serializable {
    private static final long serialVersionUID = -6690165031938265040L;
    private static final Logger logger = LoggerFactory.getLogger(TransactionOutput.class);

    /** the amount of coin */
    private double value;
    /** the pubkey of receiver */
    private SHA256Hash pubkey;

    public TransactionOutput() {
        setNull();
    }

    public TransactionOutput(double value, SHA256Hash pubkey) {
        this.value = value;
        this.pubkey = pubkey;
    }

    public void setNull() {
        value = -1;
        pubkey = SHA256Hash.ZERO_HASH;
    }

    public boolean isNull() {
        return value == -1;
    }

    public SHA256Hash getPubkey() {
        return pubkey;
    }

    public void setPubkey(SHA256Hash pubkey) {
        this.pubkey = pubkey;
    }

    public double getValue() {
        return value;
    }

    public void setValue(double value) {
        this.value = value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;

        if (o == null || getClass() != o.getClass()) return false;

        TransactionOutput that = (TransactionOutput) o;

        return new EqualsBuilder()
                .append(value, that.value)
                .append(pubkey, that.pubkey)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
                .append(value)
                .append(pubkey)
                .toHashCode();
    }
}
