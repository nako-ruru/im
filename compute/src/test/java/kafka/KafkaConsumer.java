package kafka;

import kafka.consumer.ConsumerConfig;
import kafka.consumer.ConsumerIterator;
import kafka.consumer.KafkaStream;
import kafka.javaapi.consumer.ConsumerConnector;
import kafka.serializer.StringDecoder;
import kafka.utils.VerifiableProperties;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.springframework.scheduling.concurrent.CustomizableThreadFactory;

/**
 * Create by fengtang
 * 2015/10/8 0008
 * KafkaDemo_01
 */
public class KafkaConsumer {
    public final static String TOPIC = "testweixuan";
    
    private final ConsumerConnector consumer;
    private KafkaConsumer() {
        Properties props = new Properties();
        /**
         * zookeeper 配置
         */
        props.put("zookeeper.connect", "47.92.98.23:2181");

        /**
         * group 代表一个消费组
         */
        props.put("group.id", "jd-group");

        /**
         * zk连接超时
         */
        props.put("zookeeper.session.timeout.ms", "400000");
        props.put("zookeeper.sync.time.ms", "200");
        props.put("auto.commit.interval.ms", "1000");
        props.put("auto.offset.reset", "smallest");
        /**
         * 序列化类
         */
        props.put("serializer.class", "kafka.serializer.StringEncoder");
        ConsumerConfig config = new ConsumerConfig(props);
        consumer = kafka.consumer.Consumer.createJavaConsumerConnector(config);
    }

    void consume() {
        int threadCount = 1;
        ExecutorService executor = Executors.newFixedThreadPool(threadCount, new CustomizableThreadFactory("kafka-consumer"));
        
        Map<String, Integer> topicCountMap = new HashMap<>();
        topicCountMap.put(TOPIC, threadCount);
        StringDecoder keyDecoder = new StringDecoder(new VerifiableProperties());
        StringDecoder valueDecoder = new StringDecoder(new VerifiableProperties());
        Map<String, List<KafkaStream<String, String>>> consumerMap = consumer.createMessageStreams(topicCountMap, keyDecoder, valueDecoder);
        List<KafkaStream<String, String>> streams = consumerMap.get(TOPIC);
        for(KafkaStream<String, String> stream : streams) {
            executor.submit(() -> {
                ConsumerIterator<String, String> it = stream.iterator();
                while (it.hasNext()) {
                    String message = it.next().message();
                    System.out.println(it.next().message());
                }
            });
        }
    }

    public static void main(String[] args) {
        new KafkaConsumer().consume();
    }
}