package com.mycompany.im.compute.adapter.mq;

import com.mycompany.im.compute.application.ComputeService;
import kafka.consumer.Consumer;
import kafka.consumer.ConsumerConfig;
import kafka.consumer.ConsumerIterator;
import kafka.consumer.KafkaStream;
import kafka.javaapi.consumer.ConsumerConnector;
import kafka.serializer.StringDecoder;
import kafka.utils.VerifiableProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

/**
 * Created by Administrator on 2017/8/28.
 */
@Component
public class KafkaConsumer {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    @Resource
    private ComputeService computeService;

    public void start() {
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
        ConsumerConnector connector = Consumer.createJavaConsumerConnector(config);


        Map<String, Integer> topicCountMap = new HashMap<>();
        topicCountMap.put("connector", new Integer(1));
        StringDecoder keyDecoder = new StringDecoder(new VerifiableProperties());
        StringDecoder valueDecoder = new StringDecoder(new VerifiableProperties());
        Map<String, List<KafkaStream<String, String>>> consumerMap = connector.createMessageStreams(topicCountMap, keyDecoder, valueDecoder);
        KafkaStream<String, String> stream = consumerMap.get("connector").get(0);
        ConsumerIterator<String, String> it = stream.iterator();
        new Thread(() -> {
            while (it.hasNext()) {
                try {
                    String message = it.next().message();
                    logger.info(" [x] Received '" + message + "'");
                    computeService.compute(message);
                } catch (Exception e) {
                    logger.error("", e);
                }
            }
        }, "kafka-consumer").start();
    }

}
