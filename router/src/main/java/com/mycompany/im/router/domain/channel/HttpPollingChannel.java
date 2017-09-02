package com.mycompany.im.router.domain.channel;

import com.google.common.base.Strings;
import com.google.gson.Gson;
import com.mycompany.im.router.domain.Payload;
import com.mycompany.im.util.JedisPoolUtils;
import org.springframework.stereotype.Component;
import redis.clients.jedis.ShardedJedis;
import redis.clients.jedis.ShardedJedisPipeline;
import redis.clients.jedis.ShardedJedisPool;

/**
 * Created by Administrator on 2017/9/2.
 */
@Component
public class HttpPollingChannel implements Channel {

    @Override
    public void send(Payload message) {
        ShardedJedisPool pool = JedisPoolUtils.pool();
        try (ShardedJedis jedis = pool.getResource()) {
            ShardedJedisPipeline pipelined = jedis.pipelined();
            if(!Strings.isNullOrEmpty(message.toRoomId)) {
                pipelined.zadd(message.toRoomId, message.time, new Gson().toJson(message));
            }
            if(!Strings.isNullOrEmpty(message.toUserId)) {
                pipelined.hset("user", message.toUserId, new Gson().toJson(message));
            }
            pipelined.sync();
        }
    }

}
