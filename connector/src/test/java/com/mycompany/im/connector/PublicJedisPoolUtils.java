package com.mycompany.im.connector;

import redis.clients.jedis.JedisShardInfo;

import java.util.List;

public class PublicJedisPoolUtils extends JedisPoolUtils {

    public static List<JedisShardInfo> read() {
        return JedisPoolUtils.read();
    }

}
