package com.mycompany.im.message.adapter.persistence.redis;

import com.google.common.collect.ImmutableMap;
import com.google.gson.Gson;
import com.mycompany.im.message.domain.Message;
import com.mycompany.im.message.domain.MessageRepository;
import org.springframework.stereotype.Component;
import redis.clients.jedis.ShardedJedis;
import redis.clients.jedis.ShardedJedisPool;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by Administrator on 2017/5/29.
 */
@Component
public class RedisMessageRepository implements MessageRepository {

    @Override
    public List<Message> findByRoomIdAndGreaterThan(String roomId, Long from) {
        ShardedJedisPool pool = JedisPoolUtils.pool();
        try (ShardedJedis resource = pool.getResource()) {
            List<String> values = resource.lrange(roomId, 0, -1);
            if(from == null || from.equals(0L)) {
                //如果from为null，则只取最后10条
                values = values.subList(Math.max(0, values.size() - 10), values.size());
            }
            if(!values.isEmpty()) {
                Gson gson = new Gson();
                return values.stream()
                        .map(v -> gson.fromJson(v, Message.class))
                        .filter(m -> from <= m.getTime())
                        .collect(Collectors.toList());
            } else {
                Message message = new Message();
                message.setLevel(0);
                message.setParams(ImmutableMap.of());
                message.setRoomId(roomId);
                message.setTime(System.currentTimeMillis());
                message.setType(10001);
                message.setUserId("");
                return Arrays.asList(message);
            }
        }
    }

}
