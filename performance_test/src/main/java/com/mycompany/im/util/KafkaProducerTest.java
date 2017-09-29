package com.mycompany.im.util;

import org.apache.commons.lang3.RandomStringUtils;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;

import java.util.Properties;
import java.util.function.Function;

/**
 * Create by fengtang
 * 2015/10/8 0008
 * KafkaDemo_01
 */
public class KafkaProducerTest {
    
    public final static String TOPIC = "testweixuan";
    
    private final KafkaProducer<String, String> producer;
    private KafkaProducerTest(String bootstrapServers) {
        Properties props = new Properties();
        props.put("bootstrap.servers", bootstrapServers);
        props.put("acks", "all");
        props.put("retries", 0);
        props.put("batch.size", 65536);
        props.put("linger.ms", 1000);
        props.put("buffer.memory", 33554432);
        props.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer");
        props.put("value.serializer", "org.apache.kafka.common.serialization.StringSerializer");
        producer = new KafkaProducer<>(props);
    }

    void produce(String topic, long interval) throws InterruptedException {
        int messageNo = 0;
        while (true) {
            long start = System.currentTimeMillis();
            String key = String.format("%9d", messageNo);
            String data = key + ": " + RandomStringUtils.randomPrint(100);
            producer.send(new ProducerRecord<>(topic, key, data));
            System.out.println(data);
            messageNo++;
            Thread.sleep(Math.max(0, interval - (System.currentTimeMillis() - start)));
        }
    }

    public static void main(String[] args) throws InterruptedException {
        String bootstrapServers = getOrDefault(args, 0, Function.identity(), "47.92.68.14:9092");
        String topic = getOrDefault(args, 1, Function.identity(), TOPIC);
        long interval = getOrDefault(args, 2, Long::parseLong, 500L);
        
        new KafkaProducerTest(bootstrapServers).produce(topic, interval);
    }

    private static <T> T getOrDefault(String[] args, int i, Function<String, T> func, T defaultValue) {
        return Utils.getOrDefault(args, i, func, defaultValue);
    }
    
}