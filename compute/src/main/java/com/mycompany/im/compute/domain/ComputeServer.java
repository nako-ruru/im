package com.mycompany.im.compute.domain;

import com.google.common.collect.Multimap;
import com.google.common.collect.Multimaps;
import com.google.gson.Gson;
import com.mycompany.im.util.JedisPoolUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPubSub;
import redis.clients.jedis.ShardedJedis;
import redis.clients.jedis.ShardedJedisPool;
import redis.clients.jedis.exceptions.JedisConnectionException;

import java.net.SocketException;
import java.util.Collections;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 该类订阅从connector发来的某玩家消息，并判断该玩家是否被禁言或踢掉，如果没有则存储到redis里
 */
public class ComputeServer {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private Multimap<String, String> silencedList = Multimaps.newMultimap(new ConcurrentHashMap<>(), () -> Collections.newSetFromMap(new ConcurrentHashMap<>()));
    private Multimap<String, String> kickedList = Multimaps.newMultimap(new ConcurrentHashMap<>(), () -> Collections.newSetFromMap(new ConcurrentHashMap<>()));


    public void start() throws Exception {
        ApplicationContext applicationContext = new ClassPathXmlApplicationContext(
                "framework.xml", "performance-monitor.xml"
        );
        KeywordHandler keywordHandler = applicationContext.getBean(KeywordHandler.class);

        ShardedJedisPool pool = JedisPoolUtils.shardedJedisPool();
        do {
            try {
                Jedis jedis = JedisPoolUtils.jedis();
                jedis.subscribe(new JedisPubSub() {
                    @Override
                    public void onMessage(String channel, String message) {
                        try {
                            if("connector".equals(channel)) {
                                Message msg = new Gson().fromJson(message, Message.class);
                                if(!silencedList.containsEntry(msg.roomId, msg.userId) && !kickedList.containsEntry(msg.roomId, msg.userId)) {
                                    String oldContent = (String) msg.params.get("content");
                                    String newContent = keywordHandler.handle(oldContent);
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
                        } catch (Exception e) {
                            logger.error("", e);
                        }
                    }
                }, "room_manage_channel", "connector");
            } catch (JedisConnectionException e) {
                logger.error("", e);
                if(e.getCause() != null && e.getCause() instanceof SocketException) {
                    if("Connection reset".equals(e.getCause().getMessage())) {
                        continue;
                    }
                }
                break;
            }
        } while (true);
    }
}
