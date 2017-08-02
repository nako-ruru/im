import com.google.gson.Gson;
import com.mycompany.im.compute.domain.Payload;
import com.mycompany.im.compute.domain.SubMessage;
import org.junit.Test;
import redis.clients.jedis.Jedis;

/**
 * Created by Administrator on 2017/7/2.
 */
public class KickSilenceTest {
    
    //String host = "localhost";
    String host = "47.92.98.23";

    @Test
    public void testKick() throws InterruptedException {
        Jedis jedis = new Jedis(host, 9921);
        jedis.auth("BrightHe0");

        Payload payload = new Payload();
        payload.setAdd(true);
        payload.setRoomId("384ef06d-4bf7-435c-8548-f05b60998ae9");
        payload.setUserId("userId0");

        SubMessage msg = new SubMessage();
        msg.setType("silence");
        msg.setPayload(payload);

        jedis.publish("room_manage_channel", new Gson().toJson(msg));
    }

    @Test
    public void testSilence() throws InterruptedException {
        Jedis jedis = new Jedis("localhost", 9921);
        jedis.auth("BrightHe0");

        Payload payload = new Payload();
        payload.setAdd(true);
        payload.setRoomId("room001");
        payload.setUserId("user001");

        SubMessage msg = new SubMessage();
        msg.setType("silence");
        msg.setPayload(payload);

        jedis.publish("room_manage_channel", new Gson().toJson(msg));
    }

}
