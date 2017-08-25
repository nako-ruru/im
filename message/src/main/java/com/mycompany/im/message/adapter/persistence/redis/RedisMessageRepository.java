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

/**
 * Created by Administrator on 2017/5/29.
 */
@Component
public class RedisMessageRepository implements MessageRepository {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    @Override
    public List<Message> findByRoomIdAndGreaterThan(String roomId, long from) {
        ShardedJedisPool pool = JedisPoolUtils.pool();
        try (ShardedJedis resource = pool.getResource()) {

            if(from == 0L) {
                return newEmptyMessages(roomId);
            }

            Collection<String> roomMessageTextList = resource.hgetAll(roomId).values();
            Collection<String> worldMessageTextList = resource.hgetAll("world").values();
            Collection<Message> roomMessageList = convertAndFilter(from, roomMessageTextList);
            Collection<Message> worldMessageList = convertAndFilter(from, worldMessageTextList);

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

            for(String key : keys) {
                try {
                    Collection<String> messageTextList = resource.hgetAll(key).values();
                    Collection<Message> messageList = convert(messageTextList);

                    long now = System.currentTimeMillis();
                    for(Message message : messageList) {
                        if(message.getTime() <= now - TimeUnit.HOURS.toMillis(1)) {
                            pipelined.hdel(key, message.getMessageId());
                        }
                    }
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

    private Collection<Message> convert(Collection<String> roomMessageTextList) {
        Gson gson = new Gson();
        return roomMessageTextList.stream()
                .map(v -> gson.fromJson(v, Message.class))
                .collect(Collectors.toCollection(LinkedList::new));
    }

    private Collection<Message> convertAndFilter(long from, Collection<String> roomMessageTextList) {
        return convert(roomMessageTextList).stream()
                .filter(m -> from <= m.getTime())
                .collect(Collectors.toCollection(LinkedList::new));
    }

}
