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
                            BusinessMessage businessMessage = new Gson().fromJson(message, BusinessMessage.class);
                            if(isSendingToNormalRoom(businessMessage)) {
                                sendMessageToNormalRoom(businessMessage);
                            }
                            if(isSendingToWorld(businessMessage)) {
                                sendMessageToWorld(businessMessage);
                            }
                            if(isSendingToUser(businessMessage.getToUserId())) {
                                sendMessageToUser(businessMessage);
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

        private boolean isSendingToNormalRoom(BusinessMessage businessMessage) {
            return StringUtils.isNotBlank(businessMessage.getToRoomId()) && !StringUtils.equalsIgnoreCase("world", businessMessage.getToRoomId());
        }

        private boolean isSendingToWorld(BusinessMessage businessMessage) {
            return StringUtils.isNotBlank(businessMessage.getToRoomId()) && StringUtils.equalsIgnoreCase("world", businessMessage.getToRoomId());
        }

        private boolean isSendingToUser(String toUserId) {
            return StringUtils.isNotBlank(toUserId);
        }

        private void sendMessageToNormalRoom(BusinessMessage businessMessage) {
            SendMessageToRoomCommand command = new SendMessageToRoomCommand();
            command.setImportance(businessMessage.getImportance());
            command.setToRoomId(businessMessage.getToRoomId());
            command.setContent(businessMessage.getContent());
            sendService.send(command);
        }

        private void sendMessageToWorld(BusinessMessage businessMessage) {
            SendMessageToWorldCommand command = new SendMessageToWorldCommand();
            command.setImportance(businessMessage.getImportance());
            command.setContent(businessMessage.getContent());
            sendService.send(command);
        }

        private void sendMessageToUser(BusinessMessage businessMessage) {
            SendMessageToUserCommand command = new SendMessageToUserCommand();
            command.setImportance(businessMessage.getImportance());
            command.setToUserId(businessMessage.getToUserId());
            command.setContent(businessMessage.getContent());
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

    private static class BusinessMessage {

        private String toRoomId;
        private String toUserId;
        private String content;
        private int importance;

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

        public int getImportance() {
            return importance;
        }

        public void setImportance(int importance) {
            this.importance = importance;
        }

        @Override
        public String toString() {
            return new Gson().toJson(this);
        }
    }
    
}
