/**
 * Created By Yufan Wu
 * 2019/4/19
 */
package restapi;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import org.junit.Test;

public class ResultTest {
    @Test
    public void basicTest() {
        JSONObject json = new JSONObject();
        json.put("name", "myself");
        Result resultCode = new Result("OK", 200, json);
        System.out.println(JSON.toJSON(resultCode));
    }
}
