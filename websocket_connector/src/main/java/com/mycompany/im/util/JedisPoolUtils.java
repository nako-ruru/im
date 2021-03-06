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
                    Set<HostPortPassword> hostPortPasswords = readHostPortPasswords();
                    Set<HostAndPort> hostAndPorts = hostPortPasswords.stream()
                            .map(hpp -> new HostAndPort(hpp.host, hpp.port))
                            .collect(Collectors.toSet());
                    cluster = new JedisCluster(hostAndPorts, new JedisPoolConfig());
                }
            }
        }
        return cluster;
    }
    
    public static Jedis jedis() {
        HostPortPassword hostPortPassword = readHostPortPasswords().iterator().next();
        final Jedis jedis = new Jedis(hostPortPassword.host, hostPortPassword.port);
        if(hostPortPassword.password != null) {
            jedis.auth(hostPortPassword.password);
        }
        return jedis;
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

    private static Set<HostPortPassword> readHostPortPasswords() {
        Properties properties = new Properties();
        try (Reader in = new InputStreamReader(JedisPoolUtils.class.getResourceAsStream("/redis_pool.properties"), StandardCharsets.UTF_8)) {
            properties.load(in);
            String serverInfoText = properties.getProperty("redis");
            Set<HostPortPassword> hostAndPorts = Stream.of(serverInfoText.split("[;,]"))
                    .map(element -> element.split(":"))
                    .map((String[] info) -> new HostPortPassword(
                                info[0].trim(),
                                info.length >= 2 ? Integer.parseInt(info[1].trim()) : 6379,
                                info.length >= 3 ? info[2].trim() : null
                        )
                    )
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
                    .map((String[] info) -> {
                        JedisShardInfo jedisShardInfo = new JedisShardInfo(info[0].trim(), info.length >= 2 ? Integer.parseInt(info[1].trim()): 6379);
                        if(info.length >= 3) {
                            jedisShardInfo.setPassword(info[2].trim());
                        }
                        return jedisShardInfo;
                    })
                    .collect(Collectors.toList());
            return jedisShardInfos;
        } catch (IOException e) {
            throw new Error(e);
        }

    }
    
    private static class HostPortPassword {
        public final String host, password;
        public final int port;
        public HostPortPassword(String host, int port, String password) {
            this.host = host;
            this.port = port;
            this.password = password;
        }
        
    }

}
