/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mycompany.im.compute.domain;

import com.google.gson.Gson;
import javax.annotation.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.data.redis.core.RedisTemplate;

/**
 *
 * @author Administrator
 */
public class RoomManagementListener implements MessageListener {  

    private final Logger logger = LoggerFactory.getLogger(getClass());
    @Resource
    private RedisTemplate<String, String> redisTemplate;
    @Resource
    private ComputeKernel computeKernel;
      
    public void setRedisTemplate(RedisTemplate redisTemplate) {  
        this.redisTemplate = redisTemplate;  
    }  
  
    @Override  
    public void onMessage(Message message, byte[] pattern) {  
        byte[] bodyBytes = message.getBody();//请使用valueSerializer  
        byte[] channelBytes = message.getChannel();  
        //请参考配置文件，本例中key，value的序列化方式均为string。  
        //其中key必须为stringSerializer。和redisTemplate.convertAndSend对应  
        String body = (String)redisTemplate.getValueSerializer().deserialize(bodyBytes);  
        String channel = (String)redisTemplate.getStringSerializer().deserialize(channelBytes);  
        //...  
        
        
        logger.info(" [x] Received '" + body + "'");
        try {
            if("room_manage_channel".equals(channel)) {
                RoomManagementMessage msg = new Gson().fromJson(body, RoomManagementMessage.class);
                if("silence".equals(msg.getType())) {
                    Payload payload = msg.getPayload();
                    if(payload.isAdd()) {
                        computeKernel.addSilence(payload.getRoomId(), payload.getUserId());
                    } else {
                        computeKernel.removeSilence(payload.getRoomId(), payload.getUserId());
                    }
                }
                else if("kick".equals(msg.getType())) {
                    Payload payload = msg.getPayload();
                    if(payload.isAdd()) {
                        computeKernel.addKick(payload.getRoomId(), payload.getUserId());
                    } else {
                        computeKernel.removeSilence(payload.getRoomId(), payload.getUserId());
                    }
                }
                else if("dispose".equals(msg.getType())) {
                    Payload payload = msg.getPayload();
                    computeKernel.clearSilence(payload.getRoomId());
                }
            }
        } catch (Exception e) {
            logger.error("", e);
        }
    }
    
} 