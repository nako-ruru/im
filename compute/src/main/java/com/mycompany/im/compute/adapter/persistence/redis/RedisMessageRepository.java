package com.mycompany.im.compute.adapter.persistence.redis;

import com.mycompany.im.compute.domain.MessageRepository;
import com.mycompany.im.compute.domain.ToPollingMessage;
import com.mycompany.im.util.JedisPoolUtils;
import org.springframework.stereotype.Component;
import redis.clients.jedis.ShardedJedis;
import redis.clients.jedis.ShardedJedisPipeline;
import redis.clients.jedis.ShardedJedisPool;

import java.util.Collection;
import java.util.stream.Collectors;
import org.apache.commons.lang3.text.translate.CharSequenceTranslator;
import org.apache.commons.lang3.text.translate.EntityArrays;
import org.apache.commons.lang3.text.translate.LookupTranslator;

/**
 * Created by Administrator on 2017/9/3.
 */
@Component
public class RedisMessageRepository implements MessageRepository {

    private static final CharSequenceTranslator ESCAPE_JAVA =
            new LookupTranslator(
                    new String[][]{
                        {"\"", "\\\""},
                        {"\\", "\\\\"},
                    }
            ).with(
                    new LookupTranslator(EntityArrays.JAVA_CTRL_CHARS_ESCAPE())
            );
    
    @Override
    public void save(Collection<ToPollingMessage> msgs) {
        ShardedJedisPool pool = JedisPoolUtils.shardedJedisPool();
        try(ShardedJedis shardedJedis = pool.getResource()) {
            ShardedJedisPipeline pipelined = shardedJedis.pipelined();
            for(ToPollingMessage msg : msgs) {
                String paramText = msg.params.entrySet().stream()
                        .map(entry -> String.format("\"%s\":\"%s\"", entry.getKey(), translate(entry.getValue())))
                        .collect(Collectors.joining(",", "{", "}"));
                String jsonText = String.format("{\"messageId\":%s, \"toRoomId\":%s, \"fromUserId\":%s, \"fromNickname\":%s, \"time\":%s, \"fromLevel\":%s, \"type\":%s, \"params\":%s}",
                        "\"" + translate(msg.messageId) + "\"",
                        "\"" + translate(msg.toRoomId) + "\"",
                        "\"" + translate(msg.fromUserId) + "\"",
                        "\"" + translate(msg.fromNickname) + "\"",
                        msg.time,
                        msg.fromLevel,
                        msg.type,
                        paramText
                );
                pipelined.zadd(msg.toRoomId, msg.time, jsonText);
            }
            pipelined.sync();
        }
    }

    private static String translate(CharSequence input) {
        return ESCAPE_JAVA.translate(input);
    }

}
