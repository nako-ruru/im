package com.mycompany.im.message.adapter.spring.mvc;

import com.mycompany.im.message.application.MessageParameter;
import com.mycompany.im.message.application.MessageResult;
import com.mycompany.im.message.domain.Message;
import com.mycompany.im.message.domain.MessageRepository;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.annotation.Resource;

import java.util.List;
import java.util.stream.Collectors;
import org.springframework.web.bind.annotation.RestController;

/**
 * Created by Administrator on 2017/5/13.
 */
@RestController
public class MessageController {

    @Resource
    private MessageRepository messageRepository;
    
    @RequestMapping(value = "a", method = RequestMethod.GET)
    public List<MessageResult> topic(MessageParameter parameter) {
        List<Message> messages = messageRepository.findByRoomIdAndGreaterThan(parameter.getRoomId(), parameter.getFrom());
        return messages.stream()
                .map(MessageController::convert)
                .collect(Collectors.toList());
    }

    private static MessageResult convert(Message message) {
        return new MessageResult(
                message.getRoomId(),
                message.getUserId(),
                message.getType(),
                message.getParams(),
                message.getNickname(),
                message.getLevel(),
                message.getTime()
        );
    }

}
