package com.mycompany.im.router.adapter.mq;

import com.google.gson.Gson;
import com.mycompany.im.router.application.SendMessageToRoomCommand;
import com.mycompany.im.router.application.SendMessageToUserCommand;
import com.mycompany.im.router.application.SendMessageToWorldCommand;
import com.mycompany.im.router.application.SendService;
import org.apache.commons.lang3.StringUtils;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.errors.WakeupException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.Arrays;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Created by Administrator on 2017/8/28.
 */
@Component
public class KafkaRecv {

    private final Logger logger = LoggerFactory.getLogger(getClass());
    
    private final AtomicBoolean closed = new AtomicBoolean(false);
    
    private String topic = "business";
    private String bootstrapServers = "47.92.98.23:9092";

    private SendService sendService;

    public void start() {
        int threadCount = 2;
        for(int i = 0; i < threadCount; i++) {
            new Thread(new KafkaConsumerRunner(), "kafka-consumer-" + i).start();
        }
    }
    
    public void shutdown() {
        closed.set(true);
    }

    @Resource
    public void setSendService(SendService computeService) {
        this.sendService = computeService;
    }
    public void setBootstrapServers(String bootstrapServers) {
        this.bootstrapServers = bootstrapServers;
    }
    public void setTopic(String topic) {
        this.topic = topic;
    }

    private class KafkaConsumerRunner implements Runnable {

        private KafkaConsumer<String, String> consumer;

        @Override
        public void run() {
            try {
                consumer = createKafkaConsumer();
                consumer.subscribe(Arrays.asList(topic));
                while (!closed.get()) {
                    ConsumerRecords<String, String> records = consumer.poll(2000);
                    for (ConsumerRecord<String, String> record : records) {
                        final String message = record.value();
                        logger.debug("offset = %d, key = %s, value = %s%n", record.offset(), record.key(), record.value());
                        logger.info(" [x] Received '" + message + "'");
                        try {
                            RankBusinessMessage rankBusinessMessage = new Gson().fromJson(message, RankBusinessMessage.class);
                            if(isSendingToNormalRoom(rankBusinessMessage)) {
                                sendMessageToNormalRoom(rankBusinessMessage);
                            }
                            if(isSendingToWorld(rankBusinessMessage)) {
                                sendMessageToWorld(rankBusinessMessage);
                            }
                            if(isSendingToUser(rankBusinessMessage.getToUserId())) {
                                sendMessageToUser(rankBusinessMessage);
                            }
                        } catch(Exception e) {
                            logger.error("", e);
                        }
                    }
                }
            } catch (WakeupException e) {
                // Ignore exception if closing
                if (!closed.get()) {
                    throw e;
                }
            } finally {
                consumer.close();
            }
        }

        // Shutdown hook which can be called from a separate thread
        public void shutdown() {
            consumer.wakeup();
        }

        private boolean isSendingToNormalRoom(RankBusinessMessage rankBusinessMessage) {
            return StringUtils.isNotBlank(rankBusinessMessage.getToRoomId()) && !StringUtils.equalsIgnoreCase("world", rankBusinessMessage.getToRoomId());
        }

        private boolean isSendingToWorld(RankBusinessMessage rankBusinessMessage) {
            return StringUtils.isNotBlank(rankBusinessMessage.getToRoomId()) && StringUtils.equalsIgnoreCase("world", rankBusinessMessage.getToRoomId());
        }

        private boolean isSendingToUser(String toUserId) {
            return StringUtils.isNotBlank(toUserId);
        }

        private void sendMessageToNormalRoom(RankBusinessMessage rankBusinessMessage) {
            SendMessageToRoomCommand command = new SendMessageToRoomCommand();
            command.setRank(rankBusinessMessage.getRank());
            command.setToRoomId(rankBusinessMessage.getToRoomId());
            command.setContent(rankBusinessMessage.getContent());
            sendService.send(command);
        }

        private void sendMessageToWorld(RankBusinessMessage rankBusinessMessage) {
            SendMessageToWorldCommand command = new SendMessageToWorldCommand();
            command.setRank(rankBusinessMessage.getRank());
            command.setContent(rankBusinessMessage.getContent());
            sendService.send(command);
        }

        private void sendMessageToUser(RankBusinessMessage rankBusinessMessage) {
            SendMessageToUserCommand command = new SendMessageToUserCommand();
            command.setRank(rankBusinessMessage.getRank());
            command.setToUserId(rankBusinessMessage.getToUserId());
            command.setContent(rankBusinessMessage.getContent());
            sendService.send(command);
        }

    }

    private KafkaConsumer<String, String> createKafkaConsumer() {
        Properties props = new Properties();
        props.put("bootstrap.servers", bootstrapServers);
        props.put("group.id", "from-business");
        props.put("enable.auto.commit", "true");
        props.put("auto.commit.interval.ms", "1000");
        props.put("key.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
        props.put("value.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");

        return new KafkaConsumer<>(props);
    }

    private static class RankBusinessMessage {
        private String toRoomId;
        private String toUserId;
        private String content;
        private int rank;

        public String getToRoomId() {
            return toRoomId;
        }

        public void setToRoomId(String toRoomId) {
            this.toRoomId = toRoomId;
        }

        public String getToUserId() {
            return toUserId;
        }

        public void setToUserId(String toUserId) {
            this.toUserId = toUserId;
        }

        public String getContent() {
            return content;
        }

        public void setContent(String content) {
            this.content = content;
        }

        public int getRank() {
            return rank;
        }

        public void setRank(int rank) {
            this.rank = rank;
        }

        @Override
        public String toString() {
            return new Gson().toJson(this);
        }
    }
    
}
