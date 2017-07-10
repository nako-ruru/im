/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mycompany.im.connector;

import com.google.common.collect.MapMaker;
import com.google.gson.Gson;
import com.mycompany.im.util.JedisPoolUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import redis.clients.jedis.*;

import javax.annotation.PostConstruct;
import javax.websocket.Session;
import java.util.Collection;
import java.util.Collections;
import java.util.Objects;

/**
 *
 * @author Administrator
 */
@Component
public class Game {
     
    private final Collection<Session> sessions = Collections.newSetFromMap(new MapMaker().weakKeys().makeMap());

    public void add(Session session) {
        sessions.add(session);
    }
    public void remove(Session session) {
        sessions.remove(session);
    }
    public int onlineCount() {
        return sessions.size();
    }

    @PostConstruct
    public void init() {
        Jedis jc = JedisPoolUtils.jedis();
        new Thread(() -> {
            jc.subscribe(new JedisPubSub() {
                @Override
                public void onMessage(String channel, String message) {
                    PushMessage pushMessage = new Gson().fromJson(message, PushMessage.class);
                    sendText(pushMessage.getUserId(), message);
                }
            }, "mychannel");
        }).start();
    }

    private void sendText(String userId, String text) {
        Session session = sessions.stream()
                .filter(Objects::nonNull)
                .filter(s -> StringUtils.equals(userId, (String)s.getUserProperties().get("playerId")))
                .findAny()
                .orElse(null);
        if(session != null) {
            session.getAsyncRemote().sendText(text);
        }
    }

}
