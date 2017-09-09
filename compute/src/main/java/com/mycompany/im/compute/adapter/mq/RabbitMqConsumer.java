package com.mycompany.im.compute.adapter.mq;

import com.mycompany.im.compute.application.ComputeService;
import com.rabbitmq.client.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.io.IOException;
import java.util.Arrays;
import java.util.concurrent.TimeoutException;

/**
 * Created by Administrator on 2017/8/28.
 */
@Component
public class RabbitMqConsumer {

    private static final String QUEUE_NAME = "connector";

    private final Logger logger = LoggerFactory.getLogger(getClass());

    @Resource
    private ComputeService computeService;

    public void start() {
        new Thread(() -> {
            try {
                run();
            } catch (IOException | TimeoutException e) {
                logger.error("", e);
            }
        }, "rabbitmq-consumer").start();
    }

    private void run() throws IOException, TimeoutException {
        ConnectionFactory factory = new ConnectionFactory();
//        factory.setHost("47.92.98.23");
        factory.setHost("172.26.7.220");
        factory.setUsername("live_stream");
        factory.setPassword("BrightHe0");
        Connection connection = factory.newConnection();
        Channel channel = connection.createChannel();

        channel.queueDeclare(QUEUE_NAME, false, false, false, null);
        logger.info(" [*] Waiting for messages.");

        Consumer consumer = new DefaultConsumer(channel) {
            @Override
            public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body) throws IOException {
                String message = new String(body, "UTF-8");
                logger.info(" [x] Received '" + message + "'");
                computeService.compute(Arrays.asList(message));
            }
        };
        channel.basicConsume(QUEUE_NAME, true, consumer);
    }

}
