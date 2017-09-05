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

    public List<MessageResult> findByRoomIdAndFromGreaterThan(MessageParameter parameter) {
        List<Message> messages = messageRepository.findByRoomIdAndFromGreaterThan(parameter.getRoomId(), parameter.getFrom());
        return messages.stream()
                .map(MessageQuery::convert)
                .collect(Collectors.toList());
    }

    private static MessageResult convert(Message message) {
        return new MessageResult(
                message.getToRoomId(),
                message.getFromUserId(),
                message.getType(),
                message.getParams(),
                message.getFromNickname(),
                message.getFromLevel(),
                message.getTime()
        );
    }

}
