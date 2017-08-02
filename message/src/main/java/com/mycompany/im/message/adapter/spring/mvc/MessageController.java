package com.mycompany.im.message.adapter.spring.mvc;

import com.mycompany.im.message.application.MessageParameter;
import com.mycompany.im.message.application.MessageQuery;
import com.mycompany.im.message.application.MessageResult;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.annotation.Resource;

import java.util.List;

import org.springframework.web.bind.annotation.RestController;

/**
 * Created by Administrator on 2017/5/13.
 */
@RestController
public class MessageController {

    @Resource
    private MessageQuery messageQuery;
    
    @RequestMapping(value = "a", method = RequestMethod.GET)
    public List<MessageResult> findByRoomIdAndGreaterThan(MessageParameter parameter) {
        return messageQuery.findByRoomIdAndGreaterThan(parameter);
    }

}
