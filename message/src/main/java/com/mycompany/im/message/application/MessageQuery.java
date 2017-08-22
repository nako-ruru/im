package com.mycompany.im.message.application;

import com.mycompany.im.message.domain.Message;
import com.mycompany.im.message.domain.MessageRepository;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by Administrator on 2017/7/31.
 */
@Service
public class MessageQuery {

    @Resource
    private MessageRepository messageRepository;

    public List<MessageResult> findByRoomIdAndGreaterThan(MessageParameter parameter) {
        List<Message> messages = messageRepository.findByRoomIdAndGreaterThan(parameter.getRoomId(), parameter.getFrom());
        return messages.stream()
                .map(MessageQuery::convert)
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
