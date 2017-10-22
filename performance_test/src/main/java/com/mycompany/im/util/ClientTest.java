package com.mycompany.im.util;


import java.io.*;
import java.net.Socket;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Created by Administrator on 2017/5/28.
 */
public class ClientTest {
    
    static String DEFAULT_ADDRESS = "localhost:6000";

    public static void main(String... args) throws InterruptedException {
        int clientCount = 1;
        long interval = -1;
        Thread[] threads = new Thread[clientCount];
        for(int i = 0; i < clientCount; i++) {
            int finalI = i;
            threads[i] = new Thread(() -> {
                new Client("userId" + finalI, interval).start();
            });
            threads[i].start();
        }

        Thread.sleep(3000 * 1000L);

        for(int i = 0; i < clientCount; i++) {
            threads[i].stop();
        }
    }

    private static class Client {

        private static final String[] WORDS = {
                "主播好漂亮！",
                "我爱死你了！",
                "主播，你是哪里人？",
                "来一首青花瓷",
                "主播是假唱",
                "欢迎来到小美直播间，喜欢小美的朋友们点点关注"
        };

        private static final String[] ROOM_IDS = new String[1];
        static {
            for(int i = 0; i < ROOM_IDS.length; i++) {
                ROOM_IDS[i] = "roomId" + i;
            }
        }

        private final String userId;
        private final long interval;

        public Client(String userId, long interval) {
            this.userId = userId;
            this.interval = interval;
        }

        public void start() {
            Socket socket;
            try {
                String address = DEFAULT_ADDRESS;
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
                socket = new Socket(host, port);

                ThreadLocalRandom random = ThreadLocalRandom.current();

                DataOutputStream out = new DataOutputStream(socket.getOutputStream());
                DataInputStream in = new DataInputStream(socket.getInputStream());

                MessageUtils.pushCallback(
                        in,
                        System.out::println,
                        Exception::printStackTrace
                );

                String roomId = ROOM_IDS[random.nextInt(ROOM_IDS.length)];

                MessageUtils.register(out, userId, null, "0");
                MessageUtils.enter(out, roomId);
                out.flush();

                if(interval > 0) {
                    while(true) {
                        int level = random.nextInt(1, 100);
                        String nickname = UUID.randomUUID().toString();
                        writeRandomMessage(out, roomId, nickname, level);

                        Thread.sleep(interval);
                    }
                } else {
                    Thread.sleep(Integer.MAX_VALUE);
                }
            } catch (IOException | InterruptedException e) {
                e.printStackTrace();
            }
        }

        private static void writeRandomMessage(DataOutput out, String roomId, String nickname, int level) throws IOException {
            ThreadLocalRandom random = ThreadLocalRandom.current();
            int[] availableTypes = {1};
            int type = availableTypes[random.nextInt(availableTypes.length)];
            switch (type) {
                case 1:
                    String content = WORDS[random.nextInt(WORDS.length)];
                    MessageUtils.chat(out, roomId, content, nickname, level);
                    break;
                default:
                    break;
            }
        }
    }

}
