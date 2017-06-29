package com.mycompany.im.router.application;

import com.google.common.collect.ImmutableMap;
import com.google.gson.Gson;
import com.mycompany.im.router.domain.Message;
import com.mycompany.im.router.domain.UserConnection;
import com.mycompany.im.router.domain.RoomRepository;
import com.mycompany.im.util.JedisPoolUtils;
import org.springframework.stereotype.Service;
import redis.clients.jedis.ShardedJedis;
import redis.clients.jedis.ShardedJedisPipeline;
import redis.clients.jedis.ShardedJedisPool;

import javax.annotation.Resource;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Created by Administrator on 2017/6/26.
 */
@Service
public class SendService {

    @Resource
    private RoomRepository roomRepository;

    public void send(SendMessageToRoomCommand command) {
        /*
        Collection<UserConnection> userConnections = roomRepository.findUserConnectionsByRoomId(command.getRoomId());
        Map<String, List<UserConnection>> grouped = userConnections.stream()
                .collect(Collectors.groupingBy(UserConnection::getConnectorId));*/

        ShardedJedisPool pool = JedisPoolUtils.pool();
        Message message = new Message(command.getRoomId(), command.getModuleId(), 20000, ImmutableMap.of("content", command.getContent()), 0);
        try (ShardedJedis jedis = pool.getResource()) {
            ShardedJedisPipeline pipelined = jedis.pipelined();
            pipelined.rpush(message.roomId, new Gson().toJson(message));
            pipelined.sync();
        }
    }

    public void send(SendMessageToWorldCommand command) {
        SendMessageToRoomCommand command2 = new SendMessageToRoomCommand();
        command2.setContent(command.getContent());
        command2.setModuleId(command.getModuleId());
        command2.setRoomId("world");
        send(command2);
    }


}
