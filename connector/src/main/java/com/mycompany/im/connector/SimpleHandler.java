package com.mycompany.im.connector;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.util.Attribute;
import io.netty.util.AttributeKey;
import io.netty.util.ReferenceCountUtil;

import java.nio.charset.StandardCharsets;
import java.util.concurrent.BlockingQueue;

class SimpleHandler extends ChannelInboundHandlerAdapter {

    private static final AttributeKey<String> ATTRIBUTE_KEY_USER_ID = AttributeKey.valueOf("userId");

    private BlockingQueue<Message> messages;

    public SimpleHandler(BlockingQueue<Message> messages) {
        this.messages = messages;
    }

    @Override
    public void channelActive(final ChannelHandlerContext ctx) throws Exception {
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        ByteBuf in = (ByteBuf) msg;
        try {
            Attribute<String> attr = ctx.channel().attr(ATTRIBUTE_KEY_USER_ID);
            String userId = attr.get();

            int i = 4;
            if(userId == null) {
                synchronized (attr) {
                    userId = attr.get();
                    if(userId == null) {
                        if(in.readableBytes() >= i) {
                            int uidLength = in.getInt(0);
                            if(in.readableBytes() >= i + uidLength) {
                                in.skipBytes(i);
                                CharSequence uid = in.readCharSequence(uidLength, StandardCharsets.UTF_8);
                                attr.set(uid.toString());
                                return;
                            }
                        }
                    }
                }
            }

            if(in.readableBytes() >= i) {
                int contentLength = in.getInt(0);
                if(in.readableBytes() >= i + contentLength) {
                    in.skipBytes(i);
                    CharSequence content = in.readCharSequence(contentLength, StandardCharsets.UTF_8);
                    messages.offer(new Message(userId, content.toString()));
                }
            }
        } finally {
            ReferenceCountUtil.release(msg); // (2)
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        cause.printStackTrace();
        ctx.close();
    }



}