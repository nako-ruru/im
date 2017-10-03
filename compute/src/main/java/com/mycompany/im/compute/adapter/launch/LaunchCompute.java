package com.mycompany.im.compute.adapter.launch;

import com.mycompany.im.compute.adapter.tcp.PlainRecv;
import com.mycompany.im.compute.domain.ComputeKernel;
import org.springframework.context.support.ClassPathXmlApplicationContext;

public class LaunchCompute {

    public static void main(String[] args) throws Exception {
        ClassPathXmlApplicationContext applicationContext = new ClassPathXmlApplicationContext(
                "framework.xml", "performance-monitor.xml", "config.xml", "scheduled.xml", "redis.xml"
        );
        applicationContext.getBean(ComputeKernel.class).start();

        applicationContext.getBean(PlainRecv.class).start();
    }

}