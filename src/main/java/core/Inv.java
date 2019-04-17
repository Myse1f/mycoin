/**
 * Created By Yufan Wu
 * 2018/12/26
 */
package core;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import java.io.EOFException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Serializable;

/**
 * Inventory, representing a object
 */
public class Inv implements Serializable {
    private static final long serialVersionUID = 4570598374462714006L;

    private InvType type;
    private SHA256Hash hash;

    public Inv(InvType type, SHA256Hash hash) {
        this.type = type;
        this.hash = hash;
    }

    public static Inv readInv(ObjectInputStream in) throws ClassNotFoundException, IOException {
        return (Inv) in.readObject();
    }

    public InvType getType() {
        return type;
    }

    public void setType(InvType type) {
        this.type = type;
    }

    public SHA256Hash getHash() {
        return hash;
    }

    public void setHash(SHA256Hash hash) {
        this.hash = hash;
    }

    public enum InvType {
        UNKNOWN,
        MSG_BLOCK,
        MGS_TX
    }

    @Override
    public String toString() {
        return "Type: " + type.toString() + " Hash: " + hash.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;

        if (o == null || getClass() != o.getClass()) return false;

        Inv inv = (Inv) o;

        return new EqualsBuilder()
                .append(type, inv.type)
                .append(hash, inv.hash)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
                .append(type)
                .append(hash)
                .toHashCode();
    }
}
