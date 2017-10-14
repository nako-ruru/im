/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mycompany.im.router.adapter.mq;

import com.google.gson.Gson;
import com.mycompany.im.router.domain.Payload;
import com.mycompany.im.router.domain.channel.Push;
import java.util.Properties;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 *
 * @author Administrator
 */
@Component
public class KafkaSend implements Push {
    
    private volatile KafkaProducer producer;
    private final Object producerLock = new Object();
    
    @Value("${kafka.brokers}")
    private String bootstrapServers;
    @Value("${kafka.topic}")
    private String topic;
    
    @Override
    public void send(Payload message) {
        if(producer == null) {
            synchronized(producerLock) {
                if(producer == null) {
                    producer = newKafkaProducer();
                }
            }
        }
        ProducerRecord<String, String> record = new ProducerRecord<>(topic, new Gson().toJson(message));
        producer.send(record);
    }
    
    private KafkaProducer newKafkaProducer() {
        Properties props = new Properties();
        props.put("bootstrap.servers", bootstrapServers);
        props.put("compression.type", "gzip");
//        props.put("acks", "all");
        props.put("retries", 0);
        props.put("batch.size", 65536);
        props.put("linger.ms", 1000);
        props.put("buffer.memory", 33554432);
        props.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer");
        props.put("value.serializer", "org.apache.kafka.common.serialization.StringSerializer");
        return new KafkaProducer(props);
    }
    
}
