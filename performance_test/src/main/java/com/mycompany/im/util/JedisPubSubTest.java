package com.mycompany.im.util;

import com.google.gson.Gson;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPubSub;

import java.util.UUID;

/**
 * Created by Administrator on 2017/7/2.
 */
public class JedisPubSubTest {

    public void testPub() throws InterruptedException {
        Jedis jedis = new Jedis("localhost", 9921);
        jedis.auth("BrightHe0");

        while (true) {
            MessageUtils.Msg msg = new MessageUtils.Msg();
            msg.setContent("content---" + UUID.randomUUID().toString());
            msg.setRoomId("de6edf9f-55de-44aa-b773-97bcce3ffb04");
            jedis.publish("mychannel", new Gson().toJson(msg));
            Thread.sleep(1000L);
        }
    }

    public void testSub() throws InterruptedException {
        Jedis jedis = new Jedis("localhost", 9921);
        jedis.auth("BrightHe0");
        jedis.subscribe(new JedisPubSub() {
            @Override
            public void onMessage(String channel, String message) {
                System.out.println(message);
            }
        }, "mychannel");
        wait();
    }

}
