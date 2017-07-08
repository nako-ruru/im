/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mycompany.im.connector;

import com.google.common.collect.ImmutableMap;
import com.google.gson.Gson;
import com.mycompany.im.util.JedisPoolUtils;
import redis.clients.jedis.ShardedJedis;
import redis.clients.jedis.ShardedJedisPipeline;
import redis.clients.jedis.ShardedJedisPool;

import javax.websocket.Session;

/**
 *
 * @author Administrator
 */
public class Chat {
    
    private Session session;
    private String roomId;
    private String content;
    private String nickname;
    private int level;

    public Chat(Session session, String roomId) {
        this.session = session;
        this.roomId = roomId;
    }

    public void invoke() {
        ShardedJedisPool pool = JedisPoolUtils.shardedJedisPool();

        try (ShardedJedis jedis = pool.getResource()) {
            ShardedJedisPipeline pipelined = jedis.pipelined();

            String userId = (String) session.getUserProperties().get("userId");
            ImmutableMap<String, Object> params = ImmutableMap.of("content", this.content);
            RedisMsg redisMsg = new RedisMsg(roomId, userId, nickname, level, 1, params);
            pipelined.rpush(roomId, json(redisMsg));
            pipelined.sync();
        }
    }

    private static String json(RedisMsg c) {
        return new Gson().toJson(c);
    }
    
}
