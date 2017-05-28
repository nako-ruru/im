/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mycompany.im.connector;

import redis.clients.jedis.JedisShardInfo;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.List;
import java.util.Properties;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 *
 * @author Administrator
 */
class JedisPoolUtils {


    static List<JedisShardInfo> read() {
        Properties properties = new Properties();
        try (Reader in = new InputStreamReader(JedisPoolUtils.class.getResourceAsStream("/redis_pool.properties"), StandardCharsets.UTF_8)) {
            properties.load(in);
            String serverInfoText = properties.getProperty("serverInfo");
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
