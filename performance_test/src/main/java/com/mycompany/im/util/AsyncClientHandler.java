package com.mycompany.im.util;

import com.google.common.collect.ImmutableMap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

import java.io.IOException;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.LongPredicate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AsyncClientHandler extends ChannelInboundHandlerAdapter {
    
    private final Logger logger = LoggerFactory.getLogger(getClass());

    private static final String[] WORDS = {
            "主播好漂亮！",
            "我爱死你了！",
            "主播，你是哪里人？",
            "来一首青花瓷",
            "主播是假唱",
            "欢迎来到小美直播间，喜欢小美的朋友们点点关注"
    };

    private final String userId;
    private final ScheduledExecutorService scheduledExecutorService;
    private final long interval;
    private final String roomId;
    
    private static final MyAtomicLong atomicLong = new MyAtomicLong();

    public AsyncClientHandler(String userId, String roomId, ScheduledExecutorService scheduledExecutorService, long interval) {
        this.userId = userId;
        this.roomId = roomId;
        this.scheduledExecutorService = scheduledExecutorService;
        this.interval = interval;
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        register(ctx.channel(), userId, roomId);
        if(interval > 0) {
            scheduledExecutorService.scheduleWithFixedDelay(() -> {
                ThreadLocalRandom random = ThreadLocalRandom.current();
                int level = random.nextInt(1, 100);
                String nickname = UUID.randomUUID().toString();
                String content = WORDS[random.nextInt(WORDS.length)];

                try {
                    chat(ctx.channel(), roomId, content, nickname, level);
                } catch (IOException e) {
                    logger.error(String.format("ctx: %s", ctx.channel().localAddress()), e);
                }

            }, interval, interval, TimeUnit.MILLISECONDS);
        }
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        ByteBuf buffer = (ByteBuf) msg;
        
        int length = buffer.writerIndex() - buffer.readerIndex();
        int type = buffer.readInt();
        int contentLength = length - 4;
        byte[] bytes = new byte[contentLength];
        buffer.readBytes(bytes, 0, contentLength);
        
        buffer.release();
        
        //每1秒输出一次，避免高并发竞争CPU，lock等
        long currentTimeMillis = System.currentTimeMillis();
        boolean changed = atomicLong.checkAndSet(time -> currentTimeMillis - time > 1000L, currentTimeMillis);
        if(changed) {
            try {
                MessageUtils.handle(
                        bytes,
                        0, 
                        contentLength,
                        type,
                        m -> logger.info("{}", ImmutableMap.of("messageId", m.getMessageId(), "timeText", m.getTimeText()))
                );
            } catch (Exception e) {
                logger.error(String.format("ctx: %s", ctx.channel().localAddress()), e);
            }
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        logger.error(String.format("ctx: %s", ctx.channel().localAddress()), cause);
    }

    private static void register(Channel out, String userId, String roomId) throws IOException {
        Map<String, Object> params = map(
                "userId", userId,
                "roomId", roomId
        );
        writeMsg(out, params, 0);
        writeMsg(out, params, 4);
    }

    private static void chat(Channel out, String roomId, String content, String nickname, int level) throws IOException {
        Map<String, Object> params = map(
                "roomId", roomId,
                "content", content,
                "nickname", nickname,
                "level", level
        );
        writeMsg(out, params, 1);
        out.flush();
    }

    private static void writeMsg(Channel out, Object o, int type) throws IOException {
        byte[] bytes = MessageUtils.createMsg(o, type);
        out.writeAndFlush(Unpooled.wrappedBuffer(bytes));
    }

    private static <K, V> Map<K, V> map(Object... keyValues) {
        return MessageUtils.map(keyValues);
    }
    
    private static class MyAtomicLong extends AtomicLong {
        public boolean checkAndSet(LongPredicate p, long update) {
            while (true) {
                long cur = get();

                if (p.test(cur)) {
                    if (compareAndSet(cur, update))
                        return true;
                }
                else
                    return false;
            }
        }
    }

}