import com.google.common.collect.ImmutableMap;
import com.google.gson.Gson;
import com.mycompany.im.compute.domain.ConnectorMessage;
import redis.clients.jedis.Jedis;

import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Created by Administrator on 2017/5/28.
 */
public class ConnectorMessageTest {

    public static void main(String[] args) throws InterruptedException {

        Jedis jedis = new Jedis("localhost", 9921);
        jedis.auth("BrightHe0");

        //通过修改roomId和userId来测试消息通过与否
        while (true) {
            ConnectorMessage msg = new ConnectorMessage(
                    null, "room002",
                    "user001",
                    1,
                    ImmutableMap.of("content", UUID.randomUUID().toString()),
                    ThreadLocalRandom.current().nextInt(1, 100)
            );
            jedis.rpush("connector", new Gson().toJson(msg));
            Thread.sleep(1000L);
        }
    }

}
