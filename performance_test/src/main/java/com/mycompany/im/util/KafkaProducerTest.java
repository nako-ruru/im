package com.mycompany.im.util;

import org.apache.kafka.clients.producer.KafkaProducer;

import java.util.Properties;

/**
 * Create by fengtang
 * 2015/10/8 0008
 * KafkaDemo_01
 */
public class KafkaProducerTest {
    
    public final static String TOPIC = "testweixuan";
    
    private final KafkaProducer<String, String> producer;
    private KafkaProducerTest() {
        Properties props = new Properties();
        props.put("bootstrap.servers", " 172.26.7.221:9092");
        props.put("acks", "all");
        props.put("retries", 0);
        props.put("batch.size", 65536);
        props.put("linger.ms", 1000);
        props.put("buffer.memory", 33554432);
        props.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer");
        props.put("value.serializer", "org.apache.kafka.common.serialization.StringSerializer");
        producer = new KafkaProducer<>(props);
    }

    void produce() {
        long start = System.currentTimeMillis();
        int messageNo = 1;
        final int COUNT = 1000000;
        StringBuilder data = new StringBuilder("hello kafka message ");
        while (messageNo < COUNT) {
            data.append(messageNo);
            messageNo++;
        }
        producer.close();
        long cost = System.currentTimeMillis() - start;

        System.out.printf("cost: %d\n", cost);
    }

    public static void main(String[] args) {
        new KafkaProducerTest().produce();
    }
}