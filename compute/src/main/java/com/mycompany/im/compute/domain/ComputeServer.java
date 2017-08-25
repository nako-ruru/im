package com.mycompany.im.compute.domain;

import com.google.common.collect.Multimap;
import com.google.common.collect.Multimaps;
import com.google.gson.Gson;
import com.mycompany.im.util.JedisPoolUtils;
import com.rabbitmq.client.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPubSub;
import redis.clients.jedis.ShardedJedis;
import redis.clients.jedis.ShardedJedisPool;
import redis.clients.jedis.exceptions.JedisConnectionException;

import java.io.IOException;
import java.net.SocketException;
import java.util.Collections;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 该类订阅从connector发来的某玩家消息，并判断该玩家是否被禁言或踢掉，如果没有则存储到redis里
 */
public class ComputeServer {

    private final static String QUEUE_NAME = "connector";

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private Multimap<String, String> silencedList = Multimaps.newMultimap(new ConcurrentHashMap<>(), () -> Collections.newSetFromMap(new ConcurrentHashMap<>()));
    private Multimap<String, String> kickedList = Multimaps.newMultimap(new ConcurrentHashMap<>(), () -> Collections.newSetFromMap(new ConcurrentHashMap<>()));


    public void start() throws Exception {
        ApplicationContext applicationContext = new ClassPathXmlApplicationContext(
                "framework.xml", "performance-monitor.xml"
        );

        RoomManagementQuery roomManagementQuery = applicationContext.getBean(RoomManagementQuery.class);
        RoomManagementQuery.RoomManagementInfo roomManagementInfo = roomManagementQuery.query();
        silencedList.putAll(roomManagementInfo.getSilenceList());
        kickedList.putAll(roomManagementInfo.getKickList());

        KeywordHandler keywordHandler = applicationContext.getBean(KeywordHandler.class);

        ShardedJedisPool pool = JedisPoolUtils.shardedJedisPool();

        new Thread(() -> {
            do {
                try {
                    Jedis jedis = JedisPoolUtils.jedis();
                    jedis.subscribe(new JedisPubSub() {
                        @Override
                        public void onMessage(String channel, String message) {
                            logger.info(" [x] Received '" + message + "'");
                            try {
                                if("connector".equals(channel)) {
                                    handleIncomingMessage(message, keywordHandler, pool);
                                }
                                else if("room_manage_channel".equals(channel)) {
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

        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("127.0.0.1");
        factory.setUsername("live_stream");
        factory.setPassword("BrightHe0");
        Connection connection = factory.newConnection();
        Channel channel = connection.createChannel();

        channel.queueDeclare(QUEUE_NAME, false, false, false, null);
        logger.info(" [*] Waiting for messages.");

        Consumer consumer = new DefaultConsumer(channel) {
            @Override
            public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body) throws IOException {
                String message = new String(body, "UTF-8");
                logger.info(" [x] Received '" + message + "'");

                handleIncomingMessage(message, keywordHandler, pool);
            }
        };
        channel.basicConsume(QUEUE_NAME, true, consumer);
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
                shardedJedis.hset(msg.roomId, msg.messageId, newMessage);
            }
        }
    }
}
