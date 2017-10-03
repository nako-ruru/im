/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mycompany.im.compute.adapter.service;

import com.google.common.collect.ImmutableMap;
import com.google.gson.Gson;
import com.mycompany.im.compute.domain.ServiceRegistry;
import com.mycompany.im.util.JedisPoolUtils;
import java.io.IOException;
import java.net.Socket;
import java.util.Map;
import javax.annotation.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Service;
import redis.clients.jedis.JedisShardInfo;
import redis.clients.jedis.ShardedJedis;

/**
 *
 * @author Administrator
 */
@Service
public class RedisServiceRegistry implements ServiceRegistry, ApplicationListener<ApplicationEvent> {

    private final Logger logger = LoggerFactory.getLogger(getClass());
    
    @Resource(name = "plain.tcp.listen.port")
    private int listenPort;
    
    private String registryAddress;

    @Override
    public void register() {
        registerIfRigstryAddressResolved();
    }

    private void registerIfRigstryAddressResolved() {
        if(registryAddress != null) {
            try(ShardedJedis jedis = JedisPoolUtils.shardedJedisPool().getResource()) {
                Map map = ImmutableMap.of("registerTime", System.currentTimeMillis());
                final String value = new Gson().toJson(map);
                logger.info("register: key: " + registryAddress + "; value: " + value);
                jedis.hset("compute-servers", registryAddress, value);
            }
        }
    }
    
    private String resolveAddress() {
        return resolveIp() + ":" + listenPort;
    }
    
    private String resolveIp() {
        final JedisShardInfo shardInfo = JedisPoolUtils.shardedJedisPool().getResource().getAllShardInfo().iterator().next();
        try {
            Socket s = new Socket(shardInfo.getHost(), shardInfo.getPort());
            return s.getLocalAddress().getHostAddress();
        } catch(IOException e) {
            throw new IllegalStateException(e);
        }
    }

    @Override
    public void onApplicationEvent(ApplicationEvent event) {
        registryAddress = resolveAddress();
        logger.info("resolve ip: " + registryAddress);
    }
    
}
