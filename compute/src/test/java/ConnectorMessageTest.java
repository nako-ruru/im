import com.google.common.collect.ImmutableMap;
import com.google.gson.Gson;
import com.mycompany.im.connector.Message;
import com.mycompany.im.connector.MessageUtils;
import org.junit.Test;
import redis.clients.jedis.Jedis;

import java.io.*;
import java.net.Socket;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Created by Administrator on 2017/5/28.
 */
public class ConnectorMessageTest {

    @Test
    public void testA() throws InterruptedException {

        Jedis jedis = new Jedis("localhost", 9921);
        jedis.auth("BrightHe0");

        //通过修改roomId和userId来测试消息通过与否
        while (true) {
            Message msg = new Message(
                    "room002",
                    "user001",
                    1,
                    ImmutableMap.of("content", UUID.randomUUID().toString()),
                    ThreadLocalRandom.current().nextInt(1, 100)
            );
            jedis.publish("connector", new Gson().toJson(msg));
            Thread.sleep(ThreadLocalRandom.current().nextLong(1000L, 3000L));
        }
    }

}
