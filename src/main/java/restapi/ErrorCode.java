/**
 * Created By Yufan Wu
 * 2019/4/19
 */
package restapi;

import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.annotation.JSONField;

/**
 * Error message when resource is missing
 */
public class ErrorCode {
    @JSONField(name = "msg")
    private String message; // message about this result

    @JSONField(name = "code")
    private int code; // code represented for satus

    @JSONField(name = "data")
    private JSONObject data; // payload data

    public ErrorCode(String message, int code, JSONObject data) {
        this.message = message;
        this.code = code;
        this.data = data;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public JSONObject getData() {
        return data;
    }

    public void setData(JSONObject data) {
        this.data = data;
    }
}
