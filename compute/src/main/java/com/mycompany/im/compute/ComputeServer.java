package com.mycompany.im.compute;

import com.google.common.collect.Multimap;
import com.google.common.collect.Multimaps;
import com.google.gson.Gson;
import com.mycompany.im.compute.adapter.service.KeyWorldHandlerImpl;
import com.mycompany.im.util.JedisPoolUtils;
import redis.clients.jedis.*;

import java.util.Collections;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 该类订阅从connector发来的某玩家消息，并判断该玩家是否被禁言或踢掉，如果没有则存储到redis里
 */
public class ComputeServer {

    private Multimap<String, String> silencedList = Multimaps.newMultimap(new ConcurrentHashMap<>(), () -> Collections.newSetFromMap(new ConcurrentHashMap<>()));
    private Multimap<String, String> kickedList = Multimaps.newMultimap(new ConcurrentHashMap<>(), () -> Collections.newSetFromMap(new ConcurrentHashMap<>()));

    private KeyWorldHandler keyWorldHandler = new KeyWorldHandlerImpl();

    public void start() throws Exception {
        Jedis jedis = JedisPoolUtils.jedis();
        ShardedJedisPool pool = JedisPoolUtils.shardedJedisPool();
        jedis.subscribe(new JedisPubSub() {
            @Override
            public void onMessage(String channel, String message) {
                if("connector".equals(channel)) {
                    Message msg = new Gson().fromJson(message, Message.class);
                    if(!silencedList.containsEntry(msg.roomId, msg.userId) && !kickedList.containsEntry(msg.roomId, msg.userId)) {
                        String oldContent = (String) msg.params.get("content");
                        String newContent = keyWorldHandler.handle(oldContent);
                        String newMessage = message;
                        if(!Objects.equals(newContent, oldContent)) {
                            msg.params.put("content", newContent);
                            newMessage = new Gson().toJson(msg);
                        }
                        try(ShardedJedis shardedJedis = pool.getResource()) {
                            shardedJedis.rpush(msg.roomId, newMessage);
                        }
                    }
                }
                else if("room_manage_channel".equals(channel)) {
                    SubMessage subMessage = new Gson().fromJson(message, SubMessage.class);
                    if("silence".equals(subMessage.getType())) {
                        Payload silenceMessage = subMessage.getPayload();
                        if(silenceMessage.isAdd()) {
                            silencedList.put(silenceMessage.getRoomId(), silenceMessage.getUserId());
                        } else {
                            silencedList.remove(silenceMessage.getRoomId(), silenceMessage.getUserId());
                        }
                    }
                    else if("kick".equals(subMessage.getType())) {
                        Payload silenceMessage = subMessage.getPayload();
                        if(silenceMessage.isAdd()) {
                            kickedList.put(silenceMessage.getRoomId(), silenceMessage.getUserId());
                        } else {
                            kickedList.remove(silenceMessage.getRoomId(), silenceMessage.getUserId());
                        }
                    }
                }
            }
        }, "room_manage_channel", "connector");
    }
}
