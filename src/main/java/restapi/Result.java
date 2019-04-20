/**
 * Created By Yufan Wu
 * 2019/4/19
 */
package restapi;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.annotation.JSONField;

/**
 * Error message when resource is missing
 */
public class Result {
    @JSONField(name = "msg")
    private String message; // message about this result

    @JSONField(name = "code")
    private int code; // code represented for satus

    @JSONField(name = "data")
    private JSON data; // payload data

    private static JSONObject emptyJson = new JSONObject();

    public Result() {
        this.message = null;
        this.code = -1;
        this.data = emptyJson;
    }

    public Result(String message, int code, JSONObject data) {
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

    public JSON getData() {
        return data;
    }

    public void setData(JSON data) {
        this.data = data;
    }

    public enum ResultCode{
        /**
         * success
         */
        SUCCESS(111, "Request Successfully."),

        /**
         * Exception happen inside backend
         */
        EXCEPTION(222, "Some exception happened."),

        /**
         * Block not found
         */
        BLCOK_NOT_FOUND(333, "Block not found"),
        ;

        private int code;
        private String errMsg;

        public int getCode()
        {
            return code;
        }

        public String getErrMsg(){
            return errMsg;
        }

        ResultCode(int code, String msg)
        {
            this.code = code;
            this.errMsg = msg;

        }
    }
}
