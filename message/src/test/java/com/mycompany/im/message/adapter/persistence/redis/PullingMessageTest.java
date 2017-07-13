package com.mycompany.im.message.adapter.persistence.redis;

import com.mycompany.im.message.adapter.spring.mvc.MessageController;
import com.mycompany.im.message.application.MessageParameter;
import com.mycompany.im.message.application.MessageResult;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.annotation.Resource;
import java.util.List;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration({"classpath*:framework.xml", "classpath*:mvc.xml"})
public class PullingMessageTest {

    @Resource
    private MessageController messageController;

    @Test
    public void test7shardSimplePool() {
        MessageParameter parameter = new MessageParameter() {{
            setRoomId("12");
            setFrom(1);
        }};
        List<MessageResult> messages = messageController.topic(parameter);
        System.out.println(messages);
    }

}