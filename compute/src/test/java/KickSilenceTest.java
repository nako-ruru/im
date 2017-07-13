import com.google.gson.Gson;
import com.mycompany.im.connector.MessageUtils;
import com.mycompany.im.connector.Payload;
import com.mycompany.im.connector.SubMessage;
import org.junit.Test;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPubSub;

import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Created by Administrator on 2017/7/2.
 */
public class KickSilenceTest {

    @Test
    public void testKick() throws InterruptedException {
        Jedis jedis = new Jedis("localhost", 9921);
        jedis.auth("BrightHe0");

        Payload payload = new Payload();
        payload.setAdd(true);
        payload.setRoomId("23ad8b16-6e3b-4d29-9a31-30533a3488ea");
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
