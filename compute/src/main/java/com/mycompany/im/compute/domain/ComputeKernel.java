package com.mycompany.im.compute.domain;

import com.google.common.collect.Multimap;
import com.google.common.collect.Multimaps;
import com.google.gson.Gson;
import com.jsoniter.JsonIterator;
import com.mycompany.im.compute.adapter.persistence.redis.RedisMessageRepository;
import com.mycompany.im.util.JedisPoolUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPubSub;
import redis.clients.jedis.ShardedJedisPool;
import redis.clients.jedis.exceptions.JedisConnectionException;

import javax.annotation.Resource;
import java.net.SocketException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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
    @Resource
    private RedisMessageRepository messageRepository;

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
        }, "redis-sub").start();
    }

    public void compute(Collection<String> message) {
        handleIncomingMessage(message, keywordHandler);
    }

    private void handleIncomingMessage(Collection<String> messages, KeywordHandler keywordHandler) {
        Collection<ToPollingMessage> toPollingMessages = messages.stream()
                .map(message -> JsonIterator.deserialize(message).as(FromConnectorMessage[].class))
                .flatMap(message -> Stream.of(message))
                .filter(msg -> !silencedList.containsEntry(msg.roomId, msg.userId) && !kickedList.containsEntry(msg.roomId, msg.userId))
                .map(msg -> {
                    String oldContent = (String) msg.params.get("content");
                    String newContent = keywordHandler.handle(oldContent);
                    if(!Objects.equals(newContent, oldContent)) {
                        msg.params.put("content", newContent);
                    }
                    return msg;
                })
                .map(msg -> new ToPollingMessage(msg.messageId, msg.roomId, msg.userId, msg.nickname, msg.level, msg.type, msg.params))
                .collect(Collectors.toCollection(LinkedList::new));
        messageRepository.save(toPollingMessages);
    }
    
    private static <K, V> Multimap<K, V> newConcurrentHashMultimap() {
        return Multimaps.newMultimap(new ConcurrentHashMap<>(), () -> Collections.newSetFromMap(new ConcurrentHashMap<>()));
    }
    
}
