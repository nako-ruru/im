/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mycompany.im.router.adapter.mq;

import com.google.gson.Gson;
import com.mycompany.im.router.domain.Payload;
import com.mycompany.im.router.domain.channel.Push;
import javax.annotation.Resource;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

/**
 *
 * @author Administrator
 */
@Component
@Primary
public class RedisSend implements Push {
    
    @Resource(name = "pubSubRedisTemplate")
    private RedisTemplate<String, String> redisTemplate;

    @Override
    public void send(Payload message) {
        redisTemplate.convertAndSend("router", new Gson().toJson(message));
    }
    
}
