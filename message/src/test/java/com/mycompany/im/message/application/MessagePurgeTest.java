package com.mycompany.im.message.application;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.annotation.Resource;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration({"classpath*:framework.xml", "classpath*:mvc.xml", "classpath*:performance-monitor.xml"})
public class MessagePurgeTest {

    @Resource
    private MessagePurgeService messagePurgeService;

    @Test
    public void purge() {
        messagePurgeService.purge();
    }

}