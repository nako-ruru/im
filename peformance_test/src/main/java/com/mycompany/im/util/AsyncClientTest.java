package com.mycompany.im.util;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

/**
 * Created by Administrator on 2017/8/7.
 */
public class AsyncClientTest {

    public static void main(String[] args) throws Exception {
        int clientCount = 2;
        long interval = 1000L;
         String address = "localhost:6000";
//      String address = "47.92.98.23:6000";

        if(args.length >= 1) {
            clientCount = Integer.parseInt(args[0]);
        }
        if(args.length >= 2) {
            interval = Long.parseLong(args[1]);
        }
        if(args.length >= 3) {
            address = args[2];
        }

        int colonIndex = address.indexOf(":");
        String host;
        int port;
        if(colonIndex >= 0) {
            host = address.substring(0, colonIndex);
            port = Integer.parseInt(address.substring(colonIndex + 1));
        } else {
            host = address;
            port = 6000;
        }
        long finalInterval = interval;

        ScheduledExecutorService scheduledExecutorService = Executors.newScheduledThreadPool(Runtime.getRuntime().availableProcessors() * 2);
        Executor executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors() * 2);

        EventLoopGroup workerGroup = new NioEventLoopGroup(0, executor);

        for(int i = 0; i < clientCount; i++) {
            String userId = "userId" + i;
            try {
                Bootstrap b = new Bootstrap(); // (1)
                b.group(workerGroup); // (2)
                b.channel(NioSocketChannel.class); // (3)
                b.option(ChannelOption.SO_KEEPALIVE, true); // (4)
                b.handler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    public void initChannel(SocketChannel ch) throws Exception {
                        ch.pipeline().addLast(new AsyncClientHandler(userId, scheduledExecutorService, finalInterval));
                    }
                });

                // Start the client.
                ChannelFuture f = b.connect(host, port).sync(); // (5)

                // Wait until the connection is closed.
//                f.channel().closeFuture().sync();
            } finally {
//                workerGroup.shutdownGracefully();
            }
        }
        synchronized (AsyncClientTest.class) {
            AsyncClientTest.class.wait();
        }
    }
}