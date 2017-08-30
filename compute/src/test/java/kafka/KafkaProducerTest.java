package kafka;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;

import java.util.Properties;

/**
 * Create by fengtang
 * 2015/10/8 0008
 * KafkaDemo_01
 */
public class KafkaProducerTest {
    
    public final static String TOPIC = KafkaConsumerTest.TOPIC;
    
    private final KafkaProducer<String, String> producer;
    private KafkaProducerTest() {
        Properties props = new Properties();
        props.put("bootstrap.servers", KafkaConsumerTest.BOOTSTRAP_SERVERS);
        props.put("acks", "all");
        props.put("retries", 0);
        props.put("batch.size", 16384);
        props.put("linger.ms", 1);
        props.put("buffer.memory", 33554432);
        props.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer");
        props.put("value.serializer", "org.apache.kafka.common.serialization.StringSerializer");
        producer = new KafkaProducer<>(props);
    }

    void produce() {
        int messageNo = 1000;
        final int COUNT = 10000;
        while (messageNo < COUNT) {
            String key = String.valueOf(messageNo);
            String data = "hello kafka message " + key;
            producer.send(new ProducerRecord<>(TOPIC, key, data));
            System.out.println(data);
            messageNo++;
        }
        producer.close();
    }

    public static void main(String[] args) {
        new KafkaProducerTest().produce();
    }
}