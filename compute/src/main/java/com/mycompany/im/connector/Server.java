package com.mycompany.im.connector;

import com.google.common.collect.Queues;
import com.mycompany.im.util.JedisPoolUtils;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPubSub;

import java.util.concurrent.BlockingQueue;

public class Server {

    private final int port;

    public Server(int port) {
        this.port = port;
    }

    public void start() throws Exception {
        Jedis jedis = JedisPoolUtils.jedis();
        new Thread(() -> {
            jedis.subscribe(new JedisPubSub() {
                @Override
                public void onMessage(String channel, String message) {

                }
            });
        }).start();
        new Thread(() -> {
            jedis.subscribe(new JedisPubSub() {
                @Override
                public void onMessage(String channel, String message) {
                }
            }, "mychannel");
        }).start();
    }
}
