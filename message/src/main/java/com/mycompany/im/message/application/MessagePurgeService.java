package com.mycompany.im.message.application;

import com.mycompany.im.message.domain.MessageRepository;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 * Created by Administrator on 2017/8/22.
 */
@Service
public class MessagePurgeService {

    @Resource
    private MessageRepository messageRepository;

    public void purge() {
        messageRepository.purge();
    }

}
