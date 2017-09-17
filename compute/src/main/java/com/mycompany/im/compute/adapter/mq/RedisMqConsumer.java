package com.mycompany.im.compute.adapter.mq;

import com.google.gson.Gson;
import com.mycompany.im.compute.application.ComputeService;
import com.mycompany.im.compute.domain.FromConnectorMessage;
import com.mycompany.im.util.JedisPoolUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import redis.clients.jedis.ShardedJedis;
import redis.clients.jedis.exceptions.JedisConnectionException;

import javax.annotation.Resource;
import java.net.SocketException;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by Administrator on 2017/8/28.
 */
@Component
public class RedisMqConsumer {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    @Resource
    private ComputeService computeService;

    public void start() {
        new Thread(() -> {
            do {
                try {
                    ShardedJedis jedis = JedisPoolUtils.shardedJedisPool().getResource();
                    while(true) {
                        List<String> messages = jedis.blpop(0, "connector");
                        List<String> messages2 = new LinkedList<>();
                        for(int i = 1; i < messages.size(); i += 2) {
                            String message = messages.get(i);
                            logger.info(" [x] Received '" + message + "'");
                            messages2.add(message);
                        }
                        List<FromConnectorMessage> collect = messages2.stream()
                                .map(m -> new Gson().fromJson(m, FromConnectorMessage.class))
                                .collect(Collectors.toList());
                        try {
                            computeService.compute(collect);
                        } catch (Exception e) {
                            logger.error("", e);
                        }
                    }
                } catch (JedisConnectionException e) {
                    logger.error("", e);
                    if(e.getCause() != null && e.getCause() instanceof SocketException) {
                        if("Connection reset".equals(e.getCause().getMessage())) {
                            continue;
                        }
                    }
                    break;
                }
            } while (true);
        }, "redismq-consumer").start();
    }

}
