package com.mycompany.im.connector;

import com.google.gson.Gson;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.util.AttributeKey;

import java.util.concurrent.BlockingQueue;
import java.util.logging.Level;
import java.util.logging.Logger;

class SimpleHandler extends ChannelInboundHandlerAdapter {

    static final AttributeKey<String> ATTRIBUTE_KEY_USER_ID = AttributeKey.valueOf("userId");

    private final Logger logger = Logger.getLogger(getClass().getName());

    private final BlockingQueue<Message> messages;

    public SimpleHandler(BlockingQueue<Message> messages) {
        this.messages = messages;
    }

    @Override
    public void channelActive(final ChannelHandlerContext ctx) throws Exception {
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        messages.offer((Message) msg);
        logger.fine(() -> json((Message) msg));
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        logger.log(Level.SEVERE, cause.getMessage(), cause);
    }

    private String json(Message m) {
        return new Gson().toJson(m);
    }

}