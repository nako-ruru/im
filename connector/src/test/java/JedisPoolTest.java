

import com.mycompany.im.util.JedisPoolUtils;
import org.junit.Test;
import redis.clients.jedis.ShardedJedis;
import redis.clients.jedis.ShardedJedisPipeline;
import redis.clients.jedis.ShardedJedisPool;

import java.util.Set;
import java.util.stream.Collectors;

public class JedisPoolTest {

    @Test
    public void ztest() {
        ShardedJedisPool pool = JedisPoolUtils.pool();

        ShardedJedis one = pool.getResource();
        ShardedJedisPipeline pipelined1 = one.pipelined();

        Set<String> keys = one.getAllShards().stream()
                .flatMap(jedis -> jedis.keys("*").stream())
                .collect(Collectors.toSet());
        keys.forEach(key -> pipelined1.zremrangeByRank("a", 0, -1));
        pipelined1.sync();

        one.close();
        one = pool.getResource();
        ShardedJedisPipeline pipelined = one.pipelined();

        long start = System.currentTimeMillis();
        for (int i = 0; i < 100000; i++) {
            pipelined.zadd("a", i, "" + i);
        }
        long end = System.currentTimeMillis();
        pipelined.sync();
        System.out.println("Simple@Pool SET: " + ((end - start)/1000.0) + " seconds");

        one.close();
        one = pool.getResource();
        ShardedJedisPipeline pipelined2 = one.pipelined();

        pipelined2.zremrangeByRank("a", 0, -1001);
        pipelined2.sync();


        pool.destroy();
    }

}