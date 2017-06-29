package com.mycompany.im.message.adapter.persistence.redis;

import com.google.common.collect.ImmutableMap;
import com.google.gson.Gson;
import com.mycompany.im.message.domain.Message;
import com.mycompany.im.message.domain.MessageRepository;
import org.springframework.stereotype.Component;
import redis.clients.jedis.ShardedJedis;
import redis.clients.jedis.ShardedJedisPool;

import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Created by Administrator on 2017/5/29.
 */
@Component
public class RedisMessageRepository implements MessageRepository {

    @Override
    public List<Message> findByRoomIdAndGreaterThan(String roomId, Long from) {
        ShardedJedisPool pool = JedisPoolUtils.pool();
        try (ShardedJedis resource = pool.getResource()) {

            List<String> roomMessageTextList = resource.lrange(roomId, 0, -1);
            List<String> worldMessageTextList = resource.lrange("world", 0, -1);
            List<Message> roomMessageList = convertAndFilter(from, roomMessageTextList);
            List<Message> worldMessageText = convertAndFilter(from, worldMessageTextList);

            List<Message> values = Stream.of(roomMessageList, worldMessageText)
                    .flatMap(Collection::stream)
                    .sorted(Comparator.comparingLong(Message::getTime))
                    .collect(Collectors.toList());

            if(from == null || from.equals(0L)) {
                //如果from为null，则只取最后10条
                values = values.subList(Math.max(0, values.size() - 10), values.size());
            }
            if(!values.isEmpty()) {
                return values;
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

    private List<Message> convertAndFilter(Long from, List<String> roomMessageTextList) {
        Gson gson = new Gson();
        return roomMessageTextList.stream()
                .map(v -> gson.fromJson(v, Message.class))
                .filter(m -> from <= m.getTime())
                .collect(Collectors.toList());
    }

}
