/**
 * Created By Yufan Wu
 * 2019/4/19
 */
package restapi;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import org.junit.Test;

public class ErrorCodeTest {
    @Test
    public void basicTest() {
        JSONObject json = new JSONObject();
        json.put("name", "myself");
        ErrorCode errorCode = new ErrorCode("OK", 200, json);
        System.out.println(JSON.toJSON(errorCode));
    }
}
