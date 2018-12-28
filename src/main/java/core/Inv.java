/**
 * Created By Yufan Wu
 * 2018/12/26
 */
package core;

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
}
