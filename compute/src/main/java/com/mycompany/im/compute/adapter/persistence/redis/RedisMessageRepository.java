package com.mycompany.im.compute.adapter.persistence.redis;

import com.google.common.escape.Escaper;
import com.google.common.escape.Escapers;
import com.mycompany.im.compute.domain.MessageRepository;
import com.mycompany.im.compute.domain.ToPollingMessage;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.stream.Collectors;
import javax.annotation.Resource;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.connection.StringRedisConnection;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;

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
        StringBuilder buffer = new StringBuilder();
        redisTemplate.executePipelined((RedisConnection connection) -> {
            StringRedisConnection stringRedisConn = (StringRedisConnection) connection;
            for(ToPollingMessage msg : msgs) {
                buffer.setLength(0);
                String paramText = msg.params.entrySet().stream()
                        .map(entry -> "\"" + entry.getKey() + "\":\"" + translate(entry.getValue()) + "\"")
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
                stringRedisConn.zAdd("room-" + msg.toRoomId, msg.time, jsonText);
            }
            return null;
        });
    }

    private static String translate(String input) {
        return ESCAPER.escape(input);
    }

}
