package com.mycompany.im.connector;

import com.google.common.collect.ImmutableMap;
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

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) { // (2)
        Attribute<String> attr = ctx.channel().attr(ATTRIBUTE_KEY_USER_ID);
        String userId = attr.get();

        int headerLength = 4;
        in.markReaderIndex();
        if(userId == null) {
            synchronized (attr) {
                userId = attr.get();
                if(userId == null) {
                    if(in.readableBytes() >= headerLength) {
                        int userIdLength = in.readInt();
                        if(in.readableBytes() >= userIdLength) {
                            userId = in.readCharSequence(userIdLength, StandardCharsets.UTF_8).toString();
                            attr.set(userId);
                            logger.info(ctx.channel().id() + " bind userId: " + userId);
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
        W:
        while (in.readableBytes() >= headerLength) {
            int roomIdLength = in.readInt();
            if(in.readableBytes() >= roomIdLength) {
                CharSequence roomId = in.readCharSequence(roomIdLength, StandardCharsets.UTF_8);
                if(in.readableBytes() >= headerLength + headerLength) {
                    int level = in.readInt();
                    int type = in.readInt();
                    switch (type) {
                        case 1:
                            if(in.readableBytes() >= headerLength) {
                                int contentLength = in.readInt();
                                if(in.readableBytes() >= contentLength) {
                                    CharSequence content = in.readCharSequence(contentLength, StandardCharsets.UTF_8);
                                    in.markReaderIndex();
                                    out.add(new Message(roomId.toString(), userId, type, ImmutableMap.of("content", content), level));
                                } else {
                                    in.resetReaderIndex();
                                    break W;
                                }
                            } else {
                                in.resetReaderIndex();
                                break W;
                            }
                            break;
                        case 2:
                            in.markReaderIndex();
                            out.add(new Message(roomId.toString(), userId, type, ImmutableMap.of(), level));
                            break;
                        case 3:
                            if(in.readableBytes() >= headerLength) {
                                int giftIdLength = in.readInt();
                                if(in.readableBytes() >= giftIdLength) {
                                    CharSequence giftId = in.readCharSequence(giftIdLength, StandardCharsets.UTF_8);
                                    in.markReaderIndex();
                                    out.add(new Message(roomId.toString(), userId, type, ImmutableMap.of("giftId", giftId), level));
                                } else {
                                    in.resetReaderIndex();
                                    break W;
                                }
                            } else {
                                in.resetReaderIndex();
                                break W;
                            }
                            break;
                        case 4:
                        case 5:
                        case 6:
                            in.markReaderIndex();
                            out.add(new Message(roomId.toString(), userId, type, ImmutableMap.of(), level));
                            break;
                        default:
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