package com.mycompany.im.compute.adapter.launch;

import com.mycompany.im.compute.adapter.mq.RedisMqConsumer;
import com.mycompany.im.compute.adapter.tcp.PlainRecv;
import com.mycompany.im.compute.domain.ComputeKernel;
import org.springframework.context.support.ClassPathXmlApplicationContext;

public class LaunchCompute {

    public static void main(String[] args) throws Exception {
        ClassPathXmlApplicationContext applicationContext = new ClassPathXmlApplicationContext(
                "framework.xml", "performance-monitor.xml", /*"dubbo.xml",*/ "config.xml"
        );
        applicationContext.start();
        applicationContext.getBean(ComputeKernel.class).start();

        applicationContext.getBean(RedisMqConsumer.class).start();
        /*
        applicationContext.getBean(RabbitMqConsumer.class).start();
        applicationContext.getBean(KafkaRecv.class).start();*/
        applicationContext.getBean(PlainRecv.class).start();
    }

}