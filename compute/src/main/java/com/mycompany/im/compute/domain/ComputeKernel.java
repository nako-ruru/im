package com.mycompany.im.compute.domain;

import com.google.common.collect.Multimap;
import com.google.common.collect.Multimaps;
import com.google.gson.Gson;
import com.mycompany.im.util.JedisPoolUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPubSub;
import redis.clients.jedis.ShardedJedis;
import redis.clients.jedis.ShardedJedisPool;
import redis.clients.jedis.exceptions.JedisConnectionException;

import javax.annotation.Resource;
import java.net.SocketException;
import java.util.Collections;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 该类订阅从connector发来的某玩家消息，并判断该玩家是否被禁言或踢掉，如果没有则存储到redis里
 */
@Component
public class ComputeKernel {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private final Multimap<String, String> silencedList = newConcurrentHashMultimap();
    private final Multimap<String, String> kickedList = newConcurrentHashMultimap();

    private ShardedJedisPool pool;

    @Resource
    private KeywordHandler keywordHandler;
    @Resource
    private RoomManagementQuery roomManagementQuery;

    public void start() {
        RoomManagementQuery.RoomManagementInfo roomManagementInfo = roomManagementQuery.query();
        silencedList.putAll(roomManagementInfo.getSilenceList());
        kickedList.putAll(roomManagementInfo.getKickList());

        pool = JedisPoolUtils.shardedJedisPool();
        
        new Thread(() -> {
            Jedis jedis = JedisPoolUtils.jedis();
            do {
                try {
                    jedis.subscribe(new JedisPubSub() {
                        @Override
                        public void onMessage(String channel, String message) {
                            logger.info(" [x] Received '" + message + "'");
                            try {
                                if("room_manage_channel".equals(channel)) {
                                    RoomManagementMessage msg = new Gson().fromJson(message, RoomManagementMessage.class);
                                    if("silence".equals(msg.getType())) {
                                        Payload payload = msg.getPayload();
                                        if(payload.isAdd()) {
                                            silencedList.put(payload.getRoomId(), payload.getUserId());
                                        } else {
                                            silencedList.remove(payload.getRoomId(), payload.getUserId());
                                        }
                                    }
                                    else if("kick".equals(msg.getType())) {
                                        Payload payload = msg.getPayload();
                                        if(payload.isAdd()) {
                                            kickedList.put(payload.getRoomId(), payload.getUserId());
                                        } else {
                                            kickedList.remove(payload.getRoomId(), payload.getUserId());
                                        }
                                    }
                                    else if("dispose".equals(msg.getType())) {
                                        Payload payload = msg.getPayload();
                                        silencedList.removeAll(payload.getRoomId());
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
        }).start();
    }

    public void compute(String message) {
        handleIncomingMessage(message, keywordHandler, pool);
    }

    private void handleIncomingMessage(String message, KeywordHandler keywordHandler, ShardedJedisPool pool) {
        ConnectorMessage msg = new Gson().fromJson(message, ConnectorMessage.class);
        if(!silencedList.containsEntry(msg.roomId, msg.userId) && !kickedList.containsEntry(msg.roomId, msg.userId)) {
            String oldContent = (String) msg.params.get("content");
            String newContent = keywordHandler.handle(oldContent);
            String newMessage = message;
            if(!Objects.equals(newContent, oldContent)) {
                msg.params.put("content", newContent);
                newMessage = new Gson().toJson(msg);
            }
            try(ShardedJedis shardedJedis = pool.getResource()) {
                shardedJedis.zadd(msg.roomId, msg.time, newMessage);
            }
        }
    }
    
    private static <K, V> Multimap<K, V> newConcurrentHashMultimap() {
        return Multimaps.newMultimap(new ConcurrentHashMap<>(), () -> Collections.newSetFromMap(new ConcurrentHashMap<>()));
    }
    
}
