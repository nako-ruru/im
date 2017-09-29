package com.mycompany.im.naming.adapter.persistence.redis;


import com.google.gson.Gson;
import com.mycompany.im.naming.domain.NamingRepository;
import com.mycompany.im.util.JedisPoolUtils;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import redis.clients.jedis.ShardedJedis;

@Component
public class RedisNamingRepository implements NamingRepository {
    
    private final Logger logger = LoggerFactory.getLogger(getClass());

    @Override
    public List<String> servers(String serverType) {
        ShardedJedis resource = JedisPoolUtils.shardedJedisPool().getResource();
        Map<String, String> servers = resource.hgetAll(serverType);
        
        Collection<Map.Entry<String, NamingInfo>> available = new ArrayList();
        for(Map.Entry<String, String> entry : servers.entrySet()) {
            try {
                NamingInfo namingInfo = new Gson().fromJson(entry.getValue(), NamingInfo.class);
                available.add(new AbstractMap.SimpleImmutableEntry<>(entry.getKey(), namingInfo));
            } catch(Exception e) {
                logger.error("", e);
            }
        }
        
        long from = System.currentTimeMillis() - TimeUnit.SECONDS.toMillis(5);
        return available.stream()
                .filter(server -> server.getValue().registerTime >= from)
                .sorted(Comparator.comparing(server -> server.getValue().connectedClients))
                .limit(5)
                .map(Map.Entry::getKey)
                .collect(Collectors.toCollection(LinkedList::new));
    }
    
    private static class NamingInfo {
	long registerTime;
	int loginUsers;
	int connectedClients;
    }
    
}