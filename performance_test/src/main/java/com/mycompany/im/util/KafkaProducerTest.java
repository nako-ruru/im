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
    
    public final static String TOPIC = "router";
    
    private final KafkaProducer<String, String> producer;
    private KafkaProducerTest(String bootstrapServers) {
        Properties props = new Properties();
        props.put("bootstrap.servers", bootstrapServers);
        props.put("compression.type", "gzip");
        props.put("acks", "all");
        props.put("retries", 0);
        props.put("batch.size", 65536);
        props.put("linger.ms", 1000);
        props.put("buffer.memory", 33554432);
        props.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer");
        props.put("value.serializer", "org.apache.kafka.common.serialization.StringSerializer");
        producer = new KafkaProducer<>(props);
    }

    public static final String CONTENT = "{\"toRoomId\":\"e019b161-a740-46e0-8116-27976e0dc370\",\"time\":1506785913744,\"type\":20000,\"params\":{\"content\":\"{\\\"type\\\":1,\\\"data\\\":{\\\"gift\\\":\\\"{\\\\\\\"id\\\\\\\":10253,\\\\\\\"iconUrl\\\\\\\":\\\\\\\"http://121.42.181.209:1116/gift/icon/10253.png\\\\\\\",\\\\\\\"previewUrl\\\\\\\":\\\\\\\"http://121.42.181.209:1116/gift/preview/10253.zip\\\\\\\",\\\\\\\"name\\\\\\\":\\\\\\\"巅峰战鼓\\\\\\\",\\\\\\\"levelRequire\\\\\\\":0,\\\\\\\"serialGroup\\\\\\\":1,\\\\\\\"priceType\\\\\\\":1,\\\\\\\"price\\\\\\\":100,\\\\\\\"playType\\\\\\\":1,\\\\\\\"state\\\\\\\":0,\\\\\\\"world\\\\\\\":false,\\\\\\\"guard\\\\\\\":false,\\\\\\\"expensive\\\\\\\":false,\\\\\\\"batch\\\\\\\":true,\\\\\\\"game\\\\\\\":false}\\\",\\\"user\\\":\\\"{\\\\\\\"id\\\\\\\":8,\\\\\\\"wkopenId\\\\\\\":\\\\\\\"ff8080815c872ba9015c8767fab70001\\\\\\\",\\\\\\\"avatar\\\\\\\":\\\\\\\"http://static.huajiao.com/huajiao/faceu0616/IMG-800507_1.png\\\\\\\",\\\\\\\"nickname\\\\\\\":\\\\\\\"张如笑\\\\\\\",\\\\\\\"actorVerified\\\\\\\":true,\\\\\\\"livePoster\\\\\\\":\\\\\\\"http://static.huajiao.com/huajiao/faceu0616/IMG-800507_1.png\\\\\\\",\\\\\\\"exp\\\\\\\":1691133,\\\\\\\"level\\\\\\\":170,\\\\\\\"actorExp\\\\\\\":538544,\\\\\\\"actorLevel\\\\\\\":54}\\\",\\\"room\\\":\\\"{\\\\\\\"id\\\\\\\":\\\\\\\"49e419d0-b6b4-4acb-8ad5-8290604fad3e\\\\\\\",\\\\\\\"uid\\\\\\\":27,\\\\\\\"state\\\\\\\":5,\\\\\\\"type\\\\\\\":1,\\\\\\\"heartbeatTime\\\\\\\":1506785848340,\\\\\\\"playAddresses\\\\\\\":{\\\\\\\"27\\\\\\\":{\\\\\\\"playUrl\\\\\\\":\\\\\\\"rtmp://pili-live-rtmp.paobuma.com/wifi/live_k2ob1_27\\\\\\\",\\\\\\\"flvUrl\\\\\\\":\\\\\\\"http://pili-live-hdl.paobuma.com/wifi/live_k2ob1_27.flv\\\\\\\",\\\\\\\"hlsUrl\\\\\\\":\\\\\\\"http://pili-live-hls.paobuma.com/wifi/live_k2ob1_27.m3u8\\\\\\\",\\\\\\\"pushUrl\\\\\\\":\\\\\\\"rtmp://pili-publish.paobuma.com/wifi/live_k2ob1_27?e\\\\\\\\u003d1506821848\\\\\\\\u0026token\\\\\\\\u003dT3TMtcwJVF67ke4WMOqUhXHo-OFZNGAWnXHSpw8-:DMv6FGQK1YxAfsFIftY7XYUU1gI\\\\\\\\u003d\\\\\\\"}},\\\\\\\"createTime\\\\\\\":1506785848340,\\\\\\\"audienceCount\\\\\\\":0,\\\\\\\"medium\\\\\\\":0,\\\\\\\"subject\\\\\\\":\\\\\\\"subjiect\\\\\\\",\\\\\\\"channel\\\\\\\":\\\\\\\"aaa颜值aaaa\\\\\\\",\\\\\\\"tags\\\\\\\":[\\\\\\\"aaa户外运动aaaa\\\\\\\",\\\\\\\"aaa歌神aaa\\\\\\\"],\\\\\\\"lon\\\\\\\":120.0,\\\\\\\"lat\\\\\\\":30.0,\\\\\\\"location\\\\\\\":\\\\\\\"浙江 杭州市\\\\\\\",\\\\\\\"screenOrientation\\\\\\\":0,\\\\\\\"poster\\\\\\\":\\\\\\\"http://static.huajiao.com/huajiao/faceu0616/IMG-800507_1.png\\\\\\\",\\\\\\\"test\\\\\\\":true}\\\"}}\"}}";
    
    void produce(String topic, long interval) throws InterruptedException {
        int messageNo = 0;
        
        while (true) {
            String data = String.format("%9d:%s", messageNo, RandomStringUtils.randomPrint(100));
            System.out.println(data);
            producer.send(new ProducerRecord<>(topic, CONTENT));
            messageNo++;
            Thread.sleep(10);
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