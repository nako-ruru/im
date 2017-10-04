/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mycompany.im.util;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;

/**
 *
 * @author Administrator
 */
public class KafkaFiles {
    
    public static final String CONTENT = "{\"toRoomId\":\"28\",\"time\":1506785913744,\"type\":20000,\"params\":{\"content\":\"{\\\"type\\\":1,\\\"data\\\":{\\\"gift\\\":\\\"{\\\\\\\"id\\\\\\\":10253,\\\\\\\"iconUrl\\\\\\\":\\\\\\\"http://121.42.181.209:1116/gift/icon/10253.png\\\\\\\",\\\\\\\"previewUrl\\\\\\\":\\\\\\\"http://121.42.181.209:1116/gift/preview/10253.zip\\\\\\\",\\\\\\\"name\\\\\\\":\\\\\\\"巅峰战鼓\\\\\\\",\\\\\\\"levelRequire\\\\\\\":0,\\\\\\\"serialGroup\\\\\\\":1,\\\\\\\"priceType\\\\\\\":1,\\\\\\\"price\\\\\\\":100,\\\\\\\"playType\\\\\\\":1,\\\\\\\"state\\\\\\\":0,\\\\\\\"world\\\\\\\":false,\\\\\\\"guard\\\\\\\":false,\\\\\\\"expensive\\\\\\\":false,\\\\\\\"batch\\\\\\\":true,\\\\\\\"game\\\\\\\":false}\\\",\\\"user\\\":\\\"{\\\\\\\"id\\\\\\\":8,\\\\\\\"wkopenId\\\\\\\":\\\\\\\"ff8080815c872ba9015c8767fab70001\\\\\\\",\\\\\\\"avatar\\\\\\\":\\\\\\\"http://static.huajiao.com/huajiao/faceu0616/IMG-800507_1.png\\\\\\\",\\\\\\\"nickname\\\\\\\":\\\\\\\"张如笑\\\\\\\",\\\\\\\"actorVerified\\\\\\\":true,\\\\\\\"livePoster\\\\\\\":\\\\\\\"http://static.huajiao.com/huajiao/faceu0616/IMG-800507_1.png\\\\\\\",\\\\\\\"exp\\\\\\\":1691133,\\\\\\\"level\\\\\\\":170,\\\\\\\"actorExp\\\\\\\":538544,\\\\\\\"actorLevel\\\\\\\":54}\\\",\\\"room\\\":\\\"{\\\\\\\"id\\\\\\\":\\\\\\\"49e419d0-b6b4-4acb-8ad5-8290604fad3e\\\\\\\",\\\\\\\"uid\\\\\\\":27,\\\\\\\"state\\\\\\\":5,\\\\\\\"type\\\\\\\":1,\\\\\\\"heartbeatTime\\\\\\\":1506785848340,\\\\\\\"playAddresses\\\\\\\":{\\\\\\\"27\\\\\\\":{\\\\\\\"playUrl\\\\\\\":\\\\\\\"rtmp://pili-live-rtmp.paobuma.com/wifi/live_k2ob1_27\\\\\\\",\\\\\\\"flvUrl\\\\\\\":\\\\\\\"http://pili-live-hdl.paobuma.com/wifi/live_k2ob1_27.flv\\\\\\\",\\\\\\\"hlsUrl\\\\\\\":\\\\\\\"http://pili-live-hls.paobuma.com/wifi/live_k2ob1_27.m3u8\\\\\\\",\\\\\\\"pushUrl\\\\\\\":\\\\\\\"rtmp://pili-publish.paobuma.com/wifi/live_k2ob1_27?e\\\\\\\\u003d1506821848\\\\\\\\u0026token\\\\\\\\u003dT3TMtcwJVF67ke4WMOqUhXHo-OFZNGAWnXHSpw8-:DMv6FGQK1YxAfsFIftY7XYUU1gI\\\\\\\\u003d\\\\\\\"}},\\\\\\\"createTime\\\\\\\":1506785848340,\\\\\\\"audienceCount\\\\\\\":0,\\\\\\\"medium\\\\\\\":0,\\\\\\\"subject\\\\\\\":\\\\\\\"subjiect\\\\\\\",\\\\\\\"channel\\\\\\\":\\\\\\\"aaa颜值aaaa\\\\\\\",\\\\\\\"tags\\\\\\\":[\\\\\\\"aaa户外运动aaaa\\\\\\\",\\\\\\\"aaa歌神aaa\\\\\\\"],\\\\\\\"lon\\\\\\\":120.0,\\\\\\\"lat\\\\\\\":30.0,\\\\\\\"location\\\\\\\":\\\\\\\"浙江 杭州市\\\\\\\",\\\\\\\"screenOrientation\\\\\\\":0,\\\\\\\"poster\\\\\\\":\\\\\\\"http://static.huajiao.com/huajiao/faceu0616/IMG-800507_1.png\\\\\\\",\\\\\\\"test\\\\\\\":true}\\\"}}\"}}";
    
    public static void main(String... args) throws IOException {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter("kafka.txt"))) {
            for(int i = 0; i < 2_000_0000; i++) {
                writer.write(CONTENT);
                writer.newLine();
                if(i % 100 == 0) {
                    System.out.println(i);
                }
            }
        }
    }
}
