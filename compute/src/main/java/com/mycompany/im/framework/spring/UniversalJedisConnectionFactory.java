package com.mycompany.im.framework.spring;

import com.google.common.base.Strings;
import com.google.common.collect.ImmutableSet;
import com.google.gson.Gson;
import org.springframework.data.redis.connection.RedisClusterConfiguration;
import org.springframework.data.redis.connection.RedisSentinelConfiguration;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;

import java.lang.reflect.Field;

/**
 * Created by Administrator on 2017/10/6.
 */
public class UniversalJedisConnectionFactory extends JedisConnectionFactory {

    public void setConnectionFactoryDesc(String connectionFactoryDesc) {
        try {
            A a = new Gson().fromJson(connectionFactoryDesc, A.class);
            if(!Strings.isNullOrEmpty(a.masterName)) {
                RedisSentinelConfiguration configuration = new RedisSentinelConfiguration(a.masterName, ImmutableSet.copyOf(a.addresses));
                Field field = JedisConnectionFactory.class.getDeclaredField("sentinelConfig");
                if(!field.isAccessible()) {
                    field.setAccessible(true);
                }
                field.set(this, configuration);
            }
            else if(a.addresses.length > 1) {
                RedisClusterConfiguration configuration = new RedisClusterConfiguration(ImmutableSet.copyOf(a.addresses));
                Field field = JedisConnectionFactory.class.getDeclaredField("clusterConfig");
                if(!field.isAccessible()) {
                    field.setAccessible(true);
                }
                field.set(this, configuration);
            }
            else {
                String[] elements = a.addresses[0].split(":");
                int port = 6379;
                if(elements.length == 2) {
                    port = Integer.parseInt(elements[1].trim());
                }
                String host = elements[0].trim();
                super.setPort(port);
                super.setHostName(host);
            }
        } catch (Exception e) {
            throw new Error(e);
        }
    }

    private static class A {
        private String masterName;
        private String[] addresses;
    }

}
