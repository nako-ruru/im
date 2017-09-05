package com.mycompany.im.router.application;

import com.google.common.collect.ImmutableMap;
import com.mycompany.im.router.domain.Payload;
import com.mycompany.im.router.domain.RouteObject;
import com.mycompany.im.router.domain.Router;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 * Created by Administrator on 2017/6/26.
 */
@Service
public class SendService {

    @Resource
    private Router router;

    public void send(SendMessageToUserCommand command) {
        RouteObject routeObject = new RouteObject();
        Payload payload = new Payload(null, command.getToUserId(), 20000, ImmutableMap.of("content", command.getContent()));
        routeObject.setImportance(command.getImportance());
        routeObject.setPayload(payload);
        router.route(routeObject);
    }

    public void send(SendMessageToRoomCommand command) {
        RouteObject routeMessage = new RouteObject();
        Payload message = new Payload(command.getToRoomId(), null, 20000, ImmutableMap.of("content", command.getContent()));
        routeMessage.setImportance(command.getImportance());
        routeMessage.setPayload(message);
        router.route(routeMessage);
    }

    public void send(SendMessageToWorldCommand command) {
        SendMessageToRoomCommand command2 = new SendMessageToRoomCommand();
        command2.setContent(command.getContent());
        command2.setToRoomId("world");
        send(command2);
    }

}
