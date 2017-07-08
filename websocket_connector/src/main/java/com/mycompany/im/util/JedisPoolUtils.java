package com.mycompany.im.util;

import redis.clients.jedis.*;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 *
 * @author Administrator
 */
public class JedisPoolUtils {

    private static final Object lock = new Object();
    private static volatile JedisCluster cluster;
    private static volatile ShardedJedisPool pool;

    public static JedisCluster jedisCluster() {
        if(cluster == null) {
            synchronized (lock) {
                if(cluster == null) {
                    Set<HostAndPort> hostAndPorts = readHostAndPorts();
                    cluster = new JedisCluster(hostAndPorts);
                }
            }
        }
        return cluster;
    }

    public static ShardedJedisPool shardedJedisPool() {
        if(pool == null) {
            synchronized (lock) {
                if(pool == null) {
                    List<JedisShardInfo> jedisShardInfos = readJedisShardInfos();
                    pool = new ShardedJedisPool(new JedisPoolConfig(), jedisShardInfos);
                }
            }
        }
        return pool;
    }

    private static Set<HostAndPort> readHostAndPorts() {
        Properties properties = new Properties();
        try (Reader in = new InputStreamReader(JedisPoolUtils.class.getResourceAsStream("/redis_pool.properties"), StandardCharsets.UTF_8)) {
            properties.load(in);
            String serverInfoText = properties.getProperty("redis");
            Set<HostAndPort> hostAndPorts = Stream.of(serverInfoText.split("[;,]"))
                    .map(element -> element.split(":"))
                    .map(info -> new HostAndPort(info[0].trim(), info.length >= 2 ? Integer.parseInt(info[1].trim()) : 6379))
                    .collect(Collectors.toSet());
            return hostAndPorts;
        } catch (IOException e) {
            throw new Error(e);
        }

    }

    private static List<JedisShardInfo> readJedisShardInfos() {
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
