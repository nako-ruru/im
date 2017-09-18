package com.mycompany.im.router.domain.channel;

import com.google.gson.Gson;
import com.mycompany.im.router.domain.Payload;
import com.mycompany.im.util.JedisPoolUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.exceptions.JedisConnectionException;

/**
 * Created by Administrator on 2017/9/2.
 */
@Component
public class ConnectorPushChannel implements Channel {

    private final Logger logger = LoggerFactory.getLogger(getClass());
    
    Jedis jedis = JedisPoolUtils.jedis();
    
    @Override
    public void send(Payload message) {
        int i = 0;
        while(i ++ < 3) {
            try { 
                jedis.publish("router", new Gson().toJson(message));
                break;
            } catch (JedisConnectionException e) {
                jedis = JedisPoolUtils.jedis();
                logger.error("", e);
            }
        }
    }
    
}
