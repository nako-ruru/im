import com.google.gson.Gson;
import com.mycompany.im.connector.MessageUtils;
import org.junit.Test;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPubSub;

import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Created by Administrator on 2017/7/2.
 */
public class JedisPubSubTest {

    @Test
    public void testPub() throws InterruptedException {
        Jedis jedis = new Jedis("localhost");

        while (true) {
            MessageUtils.Msg msg = new MessageUtils.Msg();
            msg.setContent("content---" + UUID.randomUUID().toString());
            msg.setModuleId("module---" + UUID.randomUUID().toString());
            msg.setUserId("userId0");
            jedis.publish("mychannel", new Gson().toJson(msg));
            Thread.sleep(ThreadLocalRandom.current().nextLong(1000L, 3000L));
        }
    }

    @Test
    public void testSub() throws InterruptedException {
        Jedis jedis = new Jedis("localhost");
        jedis.subscribe(new JedisPubSub() {
            @Override
            public void onMessage(String channel, String message) {
                System.out.println(message);
            }
        }, "mychannel");
        wait();
    }

}