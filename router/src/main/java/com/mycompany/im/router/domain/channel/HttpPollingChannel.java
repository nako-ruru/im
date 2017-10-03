package com.mycompany.im.router.domain.channel;

import com.google.common.base.Strings;
import com.google.gson.Gson;
import com.mycompany.im.router.domain.Payload;
import javax.annotation.Resource;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

/**
 * Created by Administrator on 2017/9/2.
 */
@Component
public class HttpPollingChannel implements Channel {

    @Resource
    private StringRedisTemplate redisTemplate;
    
    @Override
    public void send(Payload message) {
        if(!Strings.isNullOrEmpty(message.toRoomId)) {
            redisTemplate.opsForZSet().add("room-" + message.toRoomId, new Gson().toJson(message), message.time);
        }
        if(!Strings.isNullOrEmpty(message.toUserId)) {
            throw new UnsupportedOperationException();
        }
    }

}
