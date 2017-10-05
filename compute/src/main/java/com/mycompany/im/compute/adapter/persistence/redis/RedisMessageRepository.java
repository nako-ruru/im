package com.mycompany.im.compute.adapter.persistence.redis;

import com.google.common.escape.Escaper;
import com.google.common.escape.Escapers;
import com.mycompany.im.compute.domain.MessageRepository;
import com.mycompany.im.compute.domain.ToPollingMessage;
import java.lang.reflect.Field;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import javax.annotation.Resource;
import org.springframework.data.redis.connection.jedis.JedisClusterConnection;
import org.springframework.data.redis.core.StringRedisTemplate;
import redis.clients.jedis.BinaryJedisCluster;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisCluster;
import redis.clients.jedis.JedisSlotBasedConnectionHandler;
import redis.clients.jedis.Pipeline;
import redis.clients.util.JedisClusterCRC16;

/**
 * Created by Administrator on 2017/9/3.
 */
@Component
public class RedisMessageRepository implements MessageRepository {

    private static final Escaper ESCAPER = Escapers.builder()
            .addEscape('"', "\\\"")
            .addEscape('\\', "\\\\")
            .addEscape('\b', "\\b")
            .addEscape('\n', "\\n")
            .addEscape('\t', "\\t")
            .addEscape('\f', "\\f")
            .addEscape('\r', "\\r")
            .build();
    
    @Resource
    private StringRedisTemplate redisTemplate;
    
    @Override
    public void save(Collection<ToPollingMessage> msgs) {
        executePipelined(msgs);
    }
    
    private void executePipelined(Collection<ToPollingMessage> msgs) {
        JedisClusterConnection jedisClusterConnection = (JedisClusterConnection)redisTemplate.getConnectionFactory().getClusterConnection();
        JedisCluster jedisCluster = jedisClusterConnection.getNativeConnection();
        JedisSlotBasedConnectionHandler jedisClusterConnectionHandler = getJedisSlotBasedConnectionHandler(jedisCluster);
        
        Map<Integer, List<ToPollingMessage>> group = msgs.stream()
                .collect(Collectors.groupingBy(msg -> JedisClusterCRC16.getSlot(msg.toRoomId)));
        
        StringBuilder buffer = new StringBuilder();
        
        for(Map.Entry<Integer, List<ToPollingMessage>> entry : group.entrySet()) {
            try (Jedis connection = jedisClusterConnectionHandler.getConnectionFromSlot(entry.getKey())) {
                Pipeline pipeline = connection.pipelined();
                
                for(ToPollingMessage msg : msgs) {
                    buffer.setLength(0);
                    String paramText = msg.params.entrySet().stream()
                            .map(paramEntry -> "\"" + paramEntry.getKey() + "\":\"" + translate(paramEntry.getValue()) + "\"")
                            .collect(Collectors.joining(",", "{", "}"));
                    buffer
                            .append("{")
                            .append("\"messageId\":").append("\"").append(translate(msg.messageId)).append("\"")
                            .append(", ")
                            .append("\"toRoomId\":").append("\"").append(translate(msg.toRoomId)).append("\"")
                            .append(", ")
                            .append("\"fromUserId\":").append("\"").append(translate(msg.fromUserId)).append("\"")
                            .append(", ")
                            .append("\"fromNickname\":").append("\"").append(translate(msg.fromNickname)).append("\"")
                            .append(", ")
                            .append("\"time\":").append(msg.time)
                            .append(", ")
                            .append("\"fromLevel\":").append(msg.fromLevel)
                            .append(", ")
                            .append("\"type\":") .append(msg.type)
                            .append(", ")
                            .append("\"params\":").append(paramText)
                            .append("}");
                    String jsonText = buffer.toString();
                    
                    pipeline.zadd("room-" + msg.toRoomId, msg.time, jsonText);
                }
                pipeline.sync();
            }
        }
    }

    private static String translate(String input) {
        return ESCAPER.escape(input);
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
