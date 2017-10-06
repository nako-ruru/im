package com.mycompany.im.naming.adapter.persistence.redis;


import com.google.gson.Gson;
import com.mycompany.im.naming.domain.NamingRepository;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import javax.annotation.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

@Component
public class RedisNamingRepository implements NamingRepository {
    
    private final Logger logger = LoggerFactory.getLogger(getClass());
    
    @Resource
    private StringRedisTemplate redisTemplate;

    @Override
    public List<NamingInfo> servers(String serverType) {
        Map<String, String> servers = redisTemplate.<String, String>opsForHash().entries(serverType);
        
        Collection<Map.Entry<String, NamingInfo>> available = new ArrayList(servers.size());
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
                .map(entry -> {
                    final NamingInfo info = entry.getValue();
                    info.address = entry.getKey();
                    return info;
                })
                .collect(Collectors.toCollection(LinkedList::new));
    }
    
}