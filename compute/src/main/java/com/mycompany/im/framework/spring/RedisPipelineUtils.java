/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mycompany.im.framework.spring;

import java.lang.reflect.Field;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.connection.jedis.JedisClusterConnection;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.RedisSerializer;
import redis.clients.jedis.BinaryJedisCluster;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisCluster;
import redis.clients.jedis.JedisSlotBasedConnectionHandler;
import redis.clients.jedis.Pipeline;
import redis.clients.util.JedisClusterCRC16;

/**
 *
 * @author Administrator
 */
public class RedisPipelineUtils {
    
    public static <E, K> void execute(RedisTemplate<K, ?> redisTemplate, Collection<E> elements, Function<E, K> keyExtractor, BiConsumer<Pipeline, E> consumer) {
        RedisConnection redisConnection = redisTemplate.getConnectionFactory().getConnection();
        
        if(redisConnection instanceof JedisClusterConnection) {
            JedisClusterConnection jedisClusterConnection = (JedisClusterConnection)redisConnection;
            JedisCluster jedisCluster = jedisClusterConnection.getNativeConnection();
            JedisSlotBasedConnectionHandler jedisClusterConnectionHandler = getJedisSlotBasedConnectionHandler(jedisCluster);

            RedisSerializer keySerializer = redisTemplate.getKeySerializer();
            Map<Integer, List<E>> group = elements.stream()
                    .collect(Collectors.groupingBy(element -> JedisClusterCRC16.getSlot(keySerializer.serialize(keyExtractor.apply(element)))));

            for(Map.Entry<Integer, List<E>> entry : group.entrySet()) {
                try (Jedis connection = jedisClusterConnectionHandler.getConnectionFromSlot(entry.getKey())) {
                    executePipelined(connection, entry.getValue(), consumer);
                }
            }
        } else {
            try(Jedis connection = (Jedis) redisConnection.getNativeConnection()) {
                executePipelined(connection, elements, consumer);
            }
        }
    }
    
    private static <E> void executePipelined(final Jedis connection, Collection<E> elements, BiConsumer<Pipeline, E> consumer) {
        Pipeline pipeline = connection.pipelined();
        elements.forEach(e -> consumer.accept(pipeline, e));
        pipeline.sync();
    }
    
    private static JedisSlotBasedConnectionHandler getJedisSlotBasedConnectionHandler(JedisCluster cluster) {
        try {
            Field field = BinaryJedisCluster.class.getDeclaredField("connectionHandler");
            if(!field.isAccessible()) {
                field.setAccessible(true);
            }
            return (JedisSlotBasedConnectionHandler) field.get(cluster);
        } catch (Exception ex) {
            throw new Error(ex);
        }
    }
    
}
