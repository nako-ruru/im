package com.mycompany.im.compute.adapter.mq;

import com.mycompany.im.compute.application.ComputeService;
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
    
    private String topic = "connector";
    private String bootstrapServers = "47.92.98.23:9092";

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
                            computeService.compute(message);
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
    }

    private KafkaConsumer<String, String> createKafkaConsumer() {
        Properties props = new Properties();
        props.put("bootstrap.servers", bootstrapServers);
        props.put("group.id", "jd-group");
        props.put("enable.auto.commit", "true");
        props.put("auto.commit.interval.ms", "1000");
        props.put("key.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
        props.put("value.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");

        return new KafkaConsumer<>(props);
    }
    
}
