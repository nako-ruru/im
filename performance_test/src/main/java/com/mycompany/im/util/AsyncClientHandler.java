package com.mycompany.im.util;

import com.google.gson.Gson;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;

public class AsyncClientHandler extends ChannelInboundHandlerAdapter {

    private static final String[] WORDS = {
            "主播好漂亮！",
            "我爱死你了！",
            "主播，你是哪里人？",
            "来一首青花瓷",
            "主播是假唱",
            "欢迎来到小美直播间，喜欢小美的朋友们点点关注"
    };

    private static final String[] ROOM_IDS = new String[2];
    static {
        for(int i = 0; i < ROOM_IDS.length; i++) {
            ROOM_IDS[i] = UUID.randomUUID().toString();
        }
    }

    private final String userId;
    private final ScheduledExecutorService scheduledExecutorService;
    private final long interval;

    public AsyncClientHandler(String userId, ScheduledExecutorService scheduledExecutorService, long interval) {
        this.userId = userId;
        this.scheduledExecutorService = scheduledExecutorService;
        this.interval = interval;
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        String roomId = ROOM_IDS[ThreadLocalRandom.current().nextInt(ROOM_IDS.length)];
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
                    e.printStackTrace();
                }

            }, interval, interval, TimeUnit.MILLISECONDS);
        }
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        cause.printStackTrace();
    }

    private static void register(Channel out, String userId, String roomId) throws IOException {
        Map<String, Object> params = map(
                "userId", userId,
                "roomId", roomId
        );
        writeMsg(out, params, 0);
    }

    private static void chat(Channel out, String roomId, String content, String nickname, int level) throws IOException {
        Map<String, Object> params = map(
                "roomId", roomId,
                "content", content,
                "nickname", nickname,
                "level", level,
                "clientTime", System.currentTimeMillis()
        );
        writeMsg(out, params, 1);
    }

    private static void writeMsg(Channel out, Object o, int type) throws IOException {
        byte[] bytes = MessageUtils.createMsg(o, type);
        out.writeAndFlush(Unpooled.wrappedBuffer(bytes));
    }

    private static <K, V> Map<K, V> map(Object... keyValues) {
        Map map = new HashMap<>();
        for(int i = 0; i < keyValues.length / 2; i++) {
            map.put(keyValues[i * 2], keyValues[i * 2 + 1]);
        }
        return map;
    }

    private static String json(Object o) {
        return new Gson().toJson(o);
    }

}