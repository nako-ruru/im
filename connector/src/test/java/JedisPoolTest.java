

import com.mycompany.im.util.JedisPoolUtils;
import redis.clients.jedis.ShardedJedis;
import redis.clients.jedis.ShardedJedisPool;

import java.util.Set;

public class JedisPoolTest {

    public static void main(String... args) {
        ShardedJedisPool pool = JedisPoolUtils.pool();

        ShardedJedis one = pool.getResource();
        Set<String> keys = one.getAllShards().iterator().next().keys("*");

        System.err.println(keys);

        pool.destroy();
    }

}