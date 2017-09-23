package com.mycompany.im.compute.adapter.persistence.redis;

import com.jsoniter.output.JsonStream;
import com.jsoniter.spi.Config;
import com.mycompany.im.compute.domain.MessageRepository;
import com.mycompany.im.compute.domain.ToPollingMessage;
import com.mycompany.im.util.JedisPoolUtils;
import org.springframework.stereotype.Component;
import redis.clients.jedis.ShardedJedis;
import redis.clients.jedis.ShardedJedisPipeline;
import redis.clients.jedis.ShardedJedisPool;

import java.util.Collection;

/**
 * Created by Administrator on 2017/9/3.
 */
@Component
public class RedisMessageRepository implements MessageRepository {

    @Override
    public void save(Collection<ToPollingMessage> msgs) {
        ShardedJedisPool pool = JedisPoolUtils.shardedJedisPool();
        try(ShardedJedis shardedJedis = pool.getResource()) {
            ShardedJedisPipeline pipelined = shardedJedis.pipelined();
            final Config config = new Config.Builder().escapeUnicode(false).build();
            for(ToPollingMessage msg : msgs) {
                pipelined.zadd(msg.toRoomId, msg.time, JsonStream.serialize(config, msgs));
            }
            pipelined.sync();
        }
    }

}
