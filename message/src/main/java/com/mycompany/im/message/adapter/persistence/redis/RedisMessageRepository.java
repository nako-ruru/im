package com.mycompany.im.message.adapter.persistence.redis;

import com.google.gson.Gson;
import com.mycompany.im.message.domain.Message;
import com.mycompany.im.message.domain.MessageRepository;
import org.springframework.stereotype.Component;
import redis.clients.jedis.ShardedJedis;
import redis.clients.jedis.ShardedJedisPool;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by Administrator on 2017/5/29.
 */
@Component
public class RedisMessageRepository implements MessageRepository {

    @Override
    public List<Message> findByRoomIdAndGreaterThan(String roomId, long from) {
        ShardedJedisPool pool = JedisPoolUtils.pool();
        try (ShardedJedis resource = pool.getResource()) {
            List<String> values = resource.lrange(roomId, 0, -1);
            Gson gson = new Gson();
            return values.stream()
                    .map(v -> gson.fromJson(v, Message.class))
                    .filter(m -> from <= m.getTime())
                    .collect(Collectors.toList());
        }
    }

}
