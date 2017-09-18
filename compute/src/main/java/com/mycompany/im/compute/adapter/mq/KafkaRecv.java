package com.mycompany.im.compute.adapter.mq;

import com.google.protobuf.InvalidProtocolBufferException;
import com.mycompany.im.compute.application.ComputeService;
import com.mycompany.im.compute.domain.FromConnectorMessage;
import com.mycompany.im.compute.domain.RoomMsgToCompute;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.errors.WakeupException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedList;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Stream;

/**
 * Created by Administrator on 2017/8/28.
 */
@Component
public class KafkaRecv {

    private final Logger logger = LoggerFactory.getLogger(getClass());
    
    private final AtomicBoolean closed = new AtomicBoolean(false);
    
    private String topic;
    private String bootstrapServers;

    private ComputeService computeService;

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
    public void setComputeService(ComputeService computeService) {
        this.computeService = computeService;
    }
    @Resource(name = "kafka.brokers")
    public void setBootstrapServers(String bootstrapServers) {
        this.bootstrapServers = bootstrapServers;
    }
    @Resource(name = "topic")
    public void setTopic(String topic) {
        this.topic = topic;
    }

    private class KafkaConsumerRunner implements Runnable {

        private KafkaConsumer<String, byte[]> consumer;

        @Override
        public void run() {
            try {
                consumer = createKafkaConsumer();
                consumer.subscribe(Arrays.asList(topic));
                while (!closed.get()) {
                    ConsumerRecords<String, byte[]> records = consumer.poll(2000);
                    Collection<FromConnectorMessage> messages = new LinkedList<>();
                    for (ConsumerRecord<String, byte[]> record : records) {
                        final byte[] data = record.value();
                        logger.debug("offset = %d, key = %s, value = %s%n", record.offset(), record.key(), record.value());
                        try {
                            RoomMsgToCompute.FromConnectorMessages fromConnectorMessages = RoomMsgToCompute.FromConnectorMessages.parseFrom(data);
                            fromConnectorMessages.getMessagesList().stream()
                                    .flatMap(Stream::of)
                                    .map(this::newMessage)
                                    .forEach(messages::add);
                        } catch (InvalidProtocolBufferException e) {
                            logger.error("", e);
                        }
                    }
                    if(!CollectionUtils.isEmpty(messages)) {
                        try {
                            computeService.compute(messages);
                        } catch(Exception e) {
                            logger.error("", e);
                        }
                    }
                }
            } catch (WakeupException e) {
                // Ignore exception if closing
                logger.error("", e);
                if (!closed.get()) {
                    throw e;
                }
            } finally {
                consumer.close();
            }
        }

        private FromConnectorMessage newMessage(RoomMsgToCompute.FromConnectorMessage m) {
            return new FromConnectorMessage(
                    m.getMessageId(), 
                    m.getRoomId(),
                    m.getUserId(),
                    m.getNickname(), 
                    m.getLevel(),
                    m.getType(),
                    m.getParamsMap(),
                    m.getTime()
            );
        }

        private KafkaConsumer<String, byte[]> createKafkaConsumer() {
            Properties props = new Properties();
            props.put("bootstrap.servers", bootstrapServers);
            props.put("group.id", "jd-group");
            props.put("enable.auto.commit", "true");
            props.put("auto.commit.interval.ms", "1000");
            props.put("max.poll.records", "100000");
            props.put("key.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
            props.put("value.deserializer", "org.apache.kafka.common.serialization.ByteArrayDeserializer");

            return new KafkaConsumer<>(props);
        }

        // Shutdown hook which can be called from a separate thread
        public void shutdown() {
            consumer.wakeup();
        }
    }
    
}
