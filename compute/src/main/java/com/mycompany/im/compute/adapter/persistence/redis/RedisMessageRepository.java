package com.mycompany.im.compute.adapter.persistence.redis;

import com.google.common.escape.Escaper;
import com.google.common.escape.Escapers;
import com.mycompany.im.compute.domain.MessageRepository;
import com.mycompany.im.compute.domain.ToPollingMessage;
import com.mycompany.im.framework.spring.RedisPipelineUtils;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.Collection;
import java.util.stream.Collectors;

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
    
    @Resource(name = "messageRedisTemplate")
    private StringRedisTemplate redisTemplate;
    
    @Override
    public void save(Collection<ToPollingMessage> msgs) {
        executePipelined(msgs);
    }
    
    private void executePipelined(Collection<ToPollingMessage> msgs) {
        StringBuilder buffer = new StringBuilder();
        
        RedisPipelineUtils.execute(redisTemplate, msgs, msg -> msg.toRoomId, (pipeline, msg) -> {
            String paramText = msg.params.entrySet().stream()
                    .map(paramEntry -> "\"" + paramEntry.getKey() + "\":\"" + translate(paramEntry.getValue()) + "\"")
                    .collect(Collectors.joining(",", "{", "}"));
            
            buffer.setLength(0);
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
        });
    }

    private static String translate(String input) {
        return ESCAPER.escape(input);
    }

}
