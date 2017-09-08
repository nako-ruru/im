package com.mycompany.im.router.domain.channel;

import com.google.gson.Gson;
import com.mycompany.im.router.domain.Payload;
import com.mycompany.im.util.JedisPoolUtils;
import org.springframework.stereotype.Component;
import redis.clients.jedis.Jedis;

/**
 * Created by Administrator on 2017/9/2.
 */
@Component
public class ConnectorPushChannel implements Channel {

    Jedis jedis = JedisPoolUtils.jedis();
    
    @Override
    public void send(Payload message) {
        jedis.publish("router", new Gson().toJson(message));
    }
    
}
