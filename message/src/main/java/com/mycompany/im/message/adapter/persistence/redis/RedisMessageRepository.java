package com.mycompany.im.message.adapter.persistence.redis;

import com.google.common.collect.ImmutableMap;
import com.google.gson.Gson;
import com.mycompany.im.message.domain.Message;
import com.mycompany.im.message.domain.MessageRepository;
import com.mycompany.im.util.JedisPoolUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import redis.clients.jedis.ShardedJedis;
import redis.clients.jedis.ShardedJedisPipeline;
import redis.clients.jedis.ShardedJedisPool;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import redis.clients.jedis.Tuple;

/**
 * Created by Administrator on 2017/5/29.
 */
@Component
public class RedisMessageRepository implements MessageRepository {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    @Override
    public List<Message> findByRoomIdAndFromGreaterThan(String roomId, long from) {
        ShardedJedisPool pool = JedisPoolUtils.pool();
        try (ShardedJedis resource = pool.getResource()) {

            if(from == 0L) {
                return newEmptyMessages(roomId);
            }

            Collection<Tuple> roomMessageTupleList = resource.zrevrangeWithScores(roomId, 0, 1000);
            Collection<Tuple> worldMessageTupleList = resource.zrevrangeWithScores("world", 0, 1000);
            Collection<Message> roomMessageList = convertAndFilter(from, roomMessageTupleList);
            Collection<Message> worldMessageList = convertAndFilter(from, worldMessageTupleList);

            List<Message> values = Stream.of(roomMessageList, worldMessageList)
                    .flatMap(Collection::stream)
                    .sorted(Comparator.comparingLong(Message::getTime))
                    .collect(Collectors.toList());

            if(!values.isEmpty()) {
                return values;
            } else {
                return newEmptyMessages(roomId);
            }
        }
    }

    @Override
    public void purge() {
        ShardedJedisPool pool = JedisPoolUtils.pool();
        try (ShardedJedis resource = pool.getResource()) {

            Set<String> keys = resource.getAllShards().stream()
                    .flatMap(jedis -> jedis.keys("*").stream())
                    .collect(Collectors.toSet());
            keys.add("world");
            
            ShardedJedisPipeline pipelined = resource.pipelined();

            long end = System.currentTimeMillis() - TimeUnit.MINUTES.toMillis(5);
            for(String key : keys) {
                try {
                    pipelined.zremrangeByScore(key, 0, end);
                } catch (Exception e) {
                    logger.error("", e);
                }
            }
                  
            pipelined.sync();
        }
    }

    private static List<Message> newEmptyMessages(String roomId) {
        Message message = new Message();
        message.setLevel(0);
        message.setParams(ImmutableMap.of());
        message.setRoomId(roomId);
        message.setTime(System.currentTimeMillis());
        message.setType(10001);
        message.setUserId("");
        return Arrays.asList(message);
    }

    private Collection<Message> convertAndFilter(long from, Collection<Tuple> roomMessageTupleList) {
        Gson gson = new Gson();
        return roomMessageTupleList.stream()
                .filter(m -> from <= m.getScore())
                .map(v -> gson.fromJson(v.getElement(), Message.class))
                .collect(Collectors.toCollection(LinkedList::new));
    }

}
