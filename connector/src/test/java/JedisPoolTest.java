

import com.mycompany.im.connector.PublicJedisPoolUtils;
import org.junit.Test;
import redis.clients.jedis.ShardedJedis;
import redis.clients.jedis.ShardedJedisPipeline;
import redis.clients.jedis.ShardedJedisPool;

import java.util.Set;
import java.util.stream.Collectors;

public class JedisPoolTest {

    @Test
    public void test7shardSimplePool() {
        ShardedJedisPool pool = PublicJedisPoolUtils.pool();

        ShardedJedis one = pool.getResource();
        ShardedJedisPipeline pipelined1 = one.pipelined();

        Set<String> keys = one.getAllShards().stream()
                .flatMap(jedis -> jedis.keys("*").stream())
                .collect(Collectors.toSet());
        keys.forEach(pipelined1::del);
        pipelined1.sync();

        one.close();
        one = pool.getResource();
        ShardedJedisPipeline pipelined = one.pipelined();

        long start = System.currentTimeMillis();
        for (int i = 0; i < 10; i++) {
            pipelined.rpush("i", "" + i);
        }
        long end = System.currentTimeMillis();
        pipelined.sync();
        pool.close();
        System.out.println("Simple@Pool SET: " + ((end - start)/1000.0) + " seconds");

        pool.destroy();
    }

}