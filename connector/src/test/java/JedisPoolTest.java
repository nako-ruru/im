

import org.junit.Test;
import redis.clients.jedis.JedisPoolConfig;
import redis.clients.jedis.JedisShardInfo;
import redis.clients.jedis.ShardedJedis;
import redis.clients.jedis.ShardedJedisPool;
import redis.clients.jedis.params.sortedset.ZAddParams;

import java.util.Arrays;
import java.util.List;

public class JedisPoolTest {

    @Test
    public void test7shardSimplePool() {
        List<JedisShardInfo> shards = Arrays.asList(
                new JedisShardInfo("localhost",6379)
        );

        ShardedJedisPool pool = new ShardedJedisPool(new JedisPoolConfig(), shards);

        ShardedJedis one = pool.getResource();
        one.close();
        one = pool.getResource();

        long start = System.currentTimeMillis();
        for (int i = 0; i < 10; i++) {
            one.rpush("i", "" + i);
        }
        long end = System.currentTimeMillis();
        pool.close();
        System.out.println("Simple@Pool SET: " + ((end - start)/1000.0) + " seconds");

        pool.destroy();
    }

}