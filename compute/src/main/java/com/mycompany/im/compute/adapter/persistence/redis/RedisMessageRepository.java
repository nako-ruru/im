package com.mycompany.im.compute.adapter.persistence.redis;

import com.google.gson.Gson;
import com.mycompany.im.compute.domain.MessageRepository;
import com.mycompany.im.compute.domain.ToPollingMessage;
import com.mycompany.im.util.JedisPoolUtils;
import org.springframework.stereotype.Component;
import redis.clients.jedis.ShardedJedis;
import redis.clients.jedis.ShardedJedisPool;

/**
 * Created by Administrator on 2017/9/3.
 */
@Component
public class RedisMessageRepository implements MessageRepository {

    @Override
    public void save(ToPollingMessage msg) {
        ShardedJedisPool pool = JedisPoolUtils.shardedJedisPool();
        try(ShardedJedis shardedJedis = pool.getResource()) {
            shardedJedis.zadd(msg.toRoomId, msg.time, new Gson().toJson(msg));
        }
    }

}
