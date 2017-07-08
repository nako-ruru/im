package com.mycompany.im.util;

import redis.clients.jedis.JedisPoolConfig;
import redis.clients.jedis.JedisShardInfo;
import redis.clients.jedis.ShardedJedisPool;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Properties;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 *
 * @author Administrator
 */
public class JedisPoolUtils {

    private static final Object lock = new Object();
    private static volatile ShardedJedisPool pool;

    public static ShardedJedisPool pool() {
        if(pool == null) {
            synchronized (lock) {
                if(pool == null) {
                    List<JedisShardInfo> jedisShardInfos = read();
                    pool = new ShardedJedisPool(new JedisPoolConfig(), jedisShardInfos);
                }
            }
        }
        return pool;
    }

    private static List<JedisShardInfo> read() {
        Properties properties = new Properties();
        try (Reader in = new InputStreamReader(JedisPoolUtils.class.getResourceAsStream("/redis_pool.properties"), StandardCharsets.UTF_8)) {
            properties.load(in);
            String serverInfoText = properties.getProperty("redis");
            List<JedisShardInfo> jedisShardInfos = Stream.of(serverInfoText.split("[;,]"))
                    .map(element -> element.split(":"))
                    .map(info -> new JedisShardInfo(info[0].trim(), info.length >= 2 ? Integer.parseInt(info[1].trim()) : 6379))
                    .collect(Collectors.toList());
            return jedisShardInfos;
        } catch (IOException e) {
            throw new Error(e);
        }

    }

}
