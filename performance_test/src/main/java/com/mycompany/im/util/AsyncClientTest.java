package com.mycompany.im.util;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;

import java.util.UUID;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.function.Function;

/**
 * Created by Administrator on 2017/8/7.
 */
public class AsyncClientTest {

    public static void main(String[] args) throws Exception {
        int clientCount = getOrDefault(args, 0, Integer::parseInt, 2);
        int roomCount = getOrDefault(args, 1, Integer::parseInt, 2);
            //String defaultAddress = "47.92.98.23:6000";
        String defaultAddress = "localhost:6000";
        String address = getOrDefault(args, 2, Function.identity(), defaultAddress);
        long interval = getOrDefault(args, 3, Long::parseLong, 1000L);

        String[] roomIds = allRoomIds(roomCount);

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
        Executor executor = Executors.newCachedThreadPool();

        EventLoopGroup workerGroup = new NioEventLoopGroup(0, executor);

        for(int i = 0; i < clientCount; i++) {
            String userId = "userId" + i;
            String roomId = roomIds[i % roomCount];
            try {
                Bootstrap b = new Bootstrap(); // (1)
                b.group(workerGroup); // (2)
                b.channel(NioSocketChannel.class); // (3)
                b.option(ChannelOption.SO_KEEPALIVE, true); // (4)
                b.handler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    public void initChannel(SocketChannel ch) throws Exception {
                        ch.pipeline().addLast(new AsyncClientHandler(userId, roomId, scheduledExecutorService, finalInterval));
                    }
                });

                // Start the client.
                ChannelFuture f = b.connect(host, port).sync(); // (5)

                /*
                if((i + 1) % 100 == 0) {
                    TimeUnit.MILLISECONDS.sleep(200L);
                }*/

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

    private static String[] allRoomIds(int roomCount) {
        String[] roomIds = new String[roomCount];
        for(int i = 0; i < roomIds.length; i++) {
            roomIds[i] = UUID.randomUUID().toString();
        }
        return roomIds;
    }

    private static <T> T getOrDefault(String[] args, int i, Function<String, T> func, T defaultValue) {
        return Utils.getOrDefault(args, i, func, defaultValue);
    }

}