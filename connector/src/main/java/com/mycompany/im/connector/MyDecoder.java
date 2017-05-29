package com.mycompany.im.connector;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import io.netty.util.Attribute;
import io.netty.util.AttributeKey;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.logging.Logger;

public class MyDecoder extends ByteToMessageDecoder { // (1)

    static final AttributeKey<String> ATTRIBUTE_KEY_USER_ID = SimpleHandler.ATTRIBUTE_KEY_USER_ID;

    private final Logger logger = Logger.getLogger(getClass().getName());

    private final ThreadLocal<byte[]> buffers = ThreadLocal.withInitial(() -> new byte[0]);

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) { // (2)
        Attribute<String> attr = ctx.channel().attr(ATTRIBUTE_KEY_USER_ID);
        String userId = attr.get();

        byte[] buffer = buffers.get();

        int headerLength = 4;
        in.markReaderIndex();
        if(userId == null) {
            synchronized (attr) {
                userId = attr.get();
                if(userId == null) {
                    if(in.readableBytes() >= headerLength) {
                        int userIdLength = in.readInt();
                        if(in.readableBytes() >= userIdLength) {
                            if(buffer.length < userIdLength) {
                                buffer = new byte[userIdLength];
                                buffers.set(buffer);
                            }
                            in.readBytes(buffer, 0, userIdLength);
                            CharSequence uid = new String(buffer, 0, userIdLength, StandardCharsets.UTF_8);
                            attr.set(uid.toString());
                            logger.info(ctx.channel().id() + " bind userId: " + uid);
                            in.markReaderIndex();
                        } else {
                            in.resetReaderIndex();
                        }
                    } else {
                        in.resetReaderIndex();
                    }
                }
            }
        }

        in.markReaderIndex();
        while (in.readableBytes() >= headerLength) {
            int roomIdLength = in.readInt();
            if(in.readableBytes() >= roomIdLength) {
                if(buffer.length < roomIdLength) {
                    buffer = new byte[roomIdLength];
                    buffers.set(buffer);
                }
                in.readBytes(buffer, 0, roomIdLength);
                CharSequence roomId = new String(buffer, 0, roomIdLength, StandardCharsets.UTF_8);
                if(in.readableBytes() >= headerLength) {
                    int contentLength = in.readInt();
                    if(in.readableBytes() >= contentLength) {
                        if(buffer.length < contentLength) {
                            buffer = new byte[contentLength];
                            buffers.set(buffer);
                        }
                        in.readBytes(buffer, 0, contentLength);
                        CharSequence content = new String(buffer, 0, contentLength, StandardCharsets.UTF_8);
                        in.markReaderIndex();
                        out.add(new Message(roomId.toString(), userId, content.toString()));
                    } else {
                        in.resetReaderIndex();
                        break;
                    }
                } else {
                    in.resetReaderIndex();
                    break;
                }
            } else {
                in.resetReaderIndex();
                break;
            }
        }
    }



}