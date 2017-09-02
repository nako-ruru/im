/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mycompany.im.router.adapter.spring.mvc;

import com.mycompany.im.router.application.SendMessageToRoomCommand;
import com.mycompany.im.router.application.SendMessageToUserCommand;
import com.mycompany.im.router.application.SendMessageToWorldCommand;
import com.mycompany.im.router.application.SendService;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

/**
 *
 * @author Administrator
 */
@RestController
public class SendController {

    @Resource
    private SendService sendService;
    
    @RequestMapping(value = "/send", method = RequestMethod.POST)
    public void send(SendMessageToRoomCommand command) {
        sendService.send(command);
    }
    @RequestMapping(value = "/sendall", method = RequestMethod.POST)
    public void send(SendMessageToWorldCommand command) {
        sendService.send(command);
    }
    @RequestMapping(value = "/sendtouser", method = RequestMethod.POST)
    public void send(SendMessageToUserCommand command) {
        sendService.send(command);
    }

}
