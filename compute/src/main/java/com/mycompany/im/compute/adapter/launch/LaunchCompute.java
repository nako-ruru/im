package com.mycompany.im.compute.adapter.launch;

import com.mycompany.im.compute.adapter.mq.KafkaRecv;
import com.mycompany.im.compute.adapter.mq.RabbitMqConsumer;
import com.mycompany.im.compute.adapter.mq.RedisMqConsumer;
import com.mycompany.im.compute.domain.ComputeKernel;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

public class LaunchCompute {

    public static void main(String[] args) throws Exception {
        ApplicationContext applicationContext = new ClassPathXmlApplicationContext(
                "framework.xml", "performance-monitor.xml"
        );
        applicationContext.getBean(ComputeKernel.class).start();

        applicationContext.getBean(RabbitMqConsumer.class).start();
        applicationContext.getBean(RedisMqConsumer.class).start();
        applicationContext.getBean(KafkaRecv.class).start();
    }

}