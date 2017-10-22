package com.mycompany.im.util;

import com.github.fge.lambdas.runnable.ThrowingRunnable;
import com.google.common.collect.ImmutableMap;
import com.wifi.live.token.manager.TokenManager;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import java.io.DataOutput;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
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
    
    
    static TokenManager tokenManager = new TokenManager();
    static {
        tokenManager.init();
    }

    public AsyncClientHandler(String userId, String roomId, ScheduledExecutorService scheduledExecutorService, long interval) {
        this.userId = userId;
        this.roomId = roomId;
        this.scheduledExecutorService = scheduledExecutorService;
        this.interval = interval;
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        String token = tokenManager.createToken(userId);
        register(ctx.channel(), userId, roomId, token);
        
        ScheduledFuture[] scheduledFutureHolder = new ScheduledFuture[1];
        scheduledFutureHolder[0] = scheduledExecutorService.scheduleAtFixedRate(new ThrowingRunnable() {
            int time = 0;
            @Override
            public void doRun() throws Throwable {
                String token = tokenManager.createToken(userId);
                refreshToken(ctx.channel(), token);
                time++;
                if(time >= 10) {
                    scheduledFutureHolder[0].cancel(true);
                }
            }
        }, TimeUnit.MINUTES.toMillis(20), TimeUnit.MINUTES.toMillis(20), TimeUnit.MILLISECONDS);
        
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

    private static void register(Channel out, String userId, String roomId, String token) throws IOException {
        DataOutput dout = createDataOput(out);
        MessageUtils.register(dout, userId, token, null);
        MessageUtils.enter(dout, roomId);
        out.flush();
    }

    private static void refreshToken(Channel out, String token) throws IOException {
        DataOutput dout = createDataOput(out);
        MessageUtils.refreshToken(dout, token);
        out.flush();
    }

    private static void chat(Channel out, String roomId, String content, String nickname, int level) throws IOException {
        DataOutput dout = createDataOput(out);
        MessageUtils.chat(dout, roomId, content, nickname, level);
        out.flush();
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

    private static DataOutput createDataOput(Channel out) {
        
        DataOutput dout = new DataOutput() {
            @Override
            public void write(int b) throws IOException {
                writeByte(b);
            }

            @Override
            public void write(byte[] b) throws IOException {
                out.write(Unpooled.wrappedBuffer(b));
            }

            @Override
            public void write(byte[] b, int off, int len) throws IOException {
                out.write(Unpooled.wrappedBuffer(b, off, len));
            }

            @Override
            public void writeBoolean(boolean v) throws IOException {
                out.write(Unpooled.copyBoolean(v));
            }

            @Override
            public void writeByte(int v) throws IOException {
                out.write(Unpooled.wrappedBuffer(new byte[]{(byte)v}));
            }

            @Override
            public void writeShort(int v) throws IOException {
                out.write(Unpooled.copyShort(v));
            }

            @Override
            public void writeChar(int v) throws IOException {
                out.write(Unpooled.copyShort(v));
            }

            @Override
            public void writeInt(int v) throws IOException {
                out.write(Unpooled.copyShort(v));
            }

            @Override
            public void writeLong(long v) throws IOException {
                out.write(Unpooled.copyLong(v));
            }

            @Override
            public void writeFloat(float v) throws IOException {
                out.write(Unpooled.copyFloat(v));
            }

            @Override
            public void writeDouble(double v) throws IOException {
                out.write(Unpooled.copyDouble(v));
            }

            @Override
            public void writeBytes(String s) throws IOException {
                int len = s.length();
                for (int i = 0; i < len; i ++) {
                    write((byte) s.charAt(i));
                }
            }

            @Override
            public void writeChars(String s) throws IOException {
                int len = s.length();
                for (int i = 0; i < len; i ++) {
                    write(s.charAt(i));
                }
            }

            @Override
            public void writeUTF(String s) throws IOException {
                byte[] bytes = s.getBytes(StandardCharsets.UTF_8);
                out.write(Unpooled.copyShort(bytes.length));
                write(bytes);
            }
        };
        return dout;
    }
    
    private static class Print {
        final static char[] HEX_ARRAY = "0123456789ABCDEF".toCharArray();
        final static char BUNDLE_SEP = ' ';

        public static String bytesToHexString(byte[] bytes, int bundleSize) {
            char[] hexChars = new char[(bytes.length * 2) + (bytes.length / bundleSize)];
            for (int j = 0, k = 1; j < bytes.length; j++, k++) {
                int v = bytes[j] & 0xFF;
                int start = (j * 2) + j / bundleSize;

                hexChars[start] = HEX_ARRAY[v >>> 4];
                hexChars[start + 1] = HEX_ARRAY[v & 0x0F];

                if ((k % bundleSize) == 0) {
                    hexChars[start + 2] = BUNDLE_SEP;
                }
            }
            return new String(hexChars).trim();

        }
    }

}