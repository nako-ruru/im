/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mycompany.im.framework.spring;

import org.springframework.data.redis.connection.RedisClusterConfiguration;
import org.springframework.data.redis.connection.RedisNode;

import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 *
 * @author Administrator
 */
public class MyRedisClusterConfiguration extends RedisClusterConfiguration {

    public void setClusterNodesDesc(String desc) {
        Set<RedisNode> nodes = toRedisNodes(desc.split("[,;]"), 6379);
        super.setClusterNodes(nodes);
    }

    static Set<RedisNode> toRedisNodes(String[] desc, int defaultPort) {
        return Stream.of(desc)
                .map(nodeText -> toRedisNode(nodeText, defaultPort))
                .collect(Collectors.toSet());
    }

    private static RedisNode toRedisNode(String nodeText, int port) {
        String[] elements = nodeText.split(":");
        if(elements.length == 2) {
            port = Integer.parseInt(elements[1].trim());
        }
        String host = elements[0].trim();
        return new RedisNode(host, port);
    }

}
