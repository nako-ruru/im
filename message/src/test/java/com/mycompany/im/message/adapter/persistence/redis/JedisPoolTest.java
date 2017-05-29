package com.mycompany.im.message.adapter.persistence.redis;

import com.mycompany.im.message.domain.Message;
import com.mycompany.im.message.domain.MessageRepository;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.annotation.Resource;
import java.util.List;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration({"classpath*:framework.xml", "classpath*:mvc.xml"})
public class JedisPoolTest {

    @Resource
    private MessageRepository messageRepository;

    @Test
    public void test7shardSimplePool() {
        List<Message> messages = messageRepository.findByRoomIdAndGreaterThan("21c88ff4-2ff0-421c-a310-cafca9379fc3", 0);
        System.out.println(messages);
    }

}