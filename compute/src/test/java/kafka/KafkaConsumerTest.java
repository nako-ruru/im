package kafka;

import com.mycompany.im.compute.adapter.mq.KafkaRecv;
import com.mycompany.im.compute.application.ComputeService;

import java.util.Collection;

/**
 * Create by fengtang
 * 2015/10/8 0008
 * KafkaDemo_01
 */
public class KafkaConsumerTest {
    public final static String TOPIC = "testweixuan";
    public static final String BOOTSTRAP_SERVERS = "47.92.98.23:9092";

    public static void main(String[] args) {
        KafkaRecv consumer = new KafkaRecv();
        consumer.setComputeService(new ComputeService() {
            @Override
            public void compute(Collection<String> message) {
                //do nothing
            }
        });
        consumer.setBootstrapServers(BOOTSTRAP_SERVERS);
        consumer.setTopic(TOPIC);
        consumer.start();
    }
}