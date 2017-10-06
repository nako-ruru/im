package com.mycompany.im.message.adapter.persistence.redis;

import com.google.common.collect.ImmutableMap;
import com.google.gson.Gson;
import com.mycompany.im.message.domain.Message;
import com.mycompany.im.message.domain.MessageRepository;
import java.lang.reflect.Field;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.annotation.Resource;
import org.springframework.data.redis.connection.jedis.JedisClusterConnection;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import redis.clients.jedis.BinaryJedisCluster;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisCluster;
import redis.clients.jedis.JedisSlotBasedConnectionHandler;
import redis.clients.jedis.Pipeline;
import redis.clients.util.JedisClusterCRC16;

/**
 * Created by Administrator on 2017/5/29.
 */
@Component
public class RedisMessageRepository implements MessageRepository {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private final int purgeRetainSize = 100;
    private final long purgeFixedDelay = TimeUnit.SECONDS.toMillis(30L);
    private final int querySize = 100;
    
    @Resource
    private StringRedisTemplate redisTemplate;

    @Override
    public List<Message> findByRoomIdAndFromGreaterThan(String roomId, long from) {
        if(from == 0L) {
            return newEmptyMessages(roomId);
        }

        ZSetOperations<String, String> opsForZSet = redisTemplate.opsForZSet();
        Collection<ZSetOperations.TypedTuple<String>> roomMessageTupleList = opsForZSet.reverseRangeWithScores("room-" + roomId, 0, querySize);
        Collection<ZSetOperations.TypedTuple<String>> worldMessageTupleList = opsForZSet.reverseRangeWithScores("room-world", 0, querySize);
        Collection<Message> roomMessageList = convertAndFilter(from, roomMessageTupleList);
        Collection<Message> worldMessageList = convertAndFilter(from, worldMessageTupleList);

        List<Message> values = Stream.of(roomMessageList, worldMessageList)
                .flatMap(Collection::stream)
                .sorted(Comparator.comparingLong(Message::getTime))
                .limit(querySize)
                .collect(Collectors.toList());

        if(!values.isEmpty()) {
            return values;
        } else {
            return newEmptyMessages(roomId);
        }
    }

    @Override
    public void purge() {
        executePipelined();
    }

    private Set<String> allRoomIdsWithWorld() {
        return redisTemplate.keys("room-*");
    }

    private static List<Message> newEmptyMessages(String roomId) {
        Message message = new Message();
        message.setFromLevel(0);
        message.setParams(ImmutableMap.of());
        message.setToRoomId(roomId);
        message.setTime(System.currentTimeMillis());
        message.setType(10001);
        message.setFromUserId("");
        return Arrays.asList(message);
    }

    private static Collection<Message> convertAndFilter(long from, Collection<ZSetOperations.TypedTuple<String>> roomMessageTupleList) {
        Gson gson = new Gson();
        return roomMessageTupleList.stream()
                .filter(m -> from <= m.getScore())
                .map(v -> gson.fromJson(v.getValue(), Message.class))
                .collect(Collectors.toCollection(LinkedList::new));
    }
    private void executePipelined() {
        JedisClusterConnection jedisClusterConnection = (JedisClusterConnection)redisTemplate.getConnectionFactory().getClusterConnection();
        JedisCluster jedisCluster = jedisClusterConnection.getNativeConnection();
        JedisSlotBasedConnectionHandler jedisClusterConnectionHandler = getJedisSlotBasedConnectionHandler(jedisCluster);
        
        Set<String> keys = allRoomIdsWithWorld();
        
        Map<Integer, List<String>> group = keys.stream()
                .collect(Collectors.groupingBy(key -> JedisClusterCRC16.getSlot(key)));
        
        long end = System.currentTimeMillis() - purgeFixedDelay;
        
        for(Map.Entry<Integer, List<String>> entry : group.entrySet()) {
            try (Jedis connection = jedisClusterConnectionHandler.getConnectionFromSlot(entry.getKey())) {
                Pipeline pipeline = connection.pipelined();
                
                for(String key : entry.getValue()) {
                    try {
                        pipeline.zremrangeByRank(key, 0, -(purgeRetainSize + 1));
                    } catch (Exception e) {
                        logger.error("", e);
                    }
                    try {
                        pipeline.zremrangeByScore(key, 0, end);
                    } catch (Exception e) {
                        logger.error("", e);
                    }
                }
                pipeline.sync();
            }
        }
    }
    
    private static JedisSlotBasedConnectionHandler getJedisSlotBasedConnectionHandler(JedisCluster cluster) {
        try {
            Field field = BinaryJedisCluster.class.getDeclaredField("connectionHandler");
            if(!field.isAccessible()) {
                field.setAccessible(true);
            }
            return (JedisSlotBasedConnectionHandler) field.get(cluster);
        } catch (Exception ex) {
            throw new Error(ex);
        }
    }

}
