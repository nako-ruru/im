package com.mycompany.im.util;

import java.io.DataInputStream;
import java.io.DataOutput;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Created by Administrator on 2017/5/28.
 */
public class ClientTest {

    public static void main(String[] args) throws InterruptedException {
        int clientCount = 2;
        long interval = 3000L;
        String address = "localhost:6000";
//       String address = "47.92.98.23:6000";

        if(args.length >= 1) {
            clientCount = Integer.parseInt(args[0]);
        }
        if(args.length >= 2) {
            interval = Long.parseLong(args[1]);
        }
        if(args.length >= 3) {
            address = args[2];
        }

        Thread[] threads = new Thread[clientCount];
        long finalInterval = interval;
        String finalAddress = address;
        for(int i = 0; i < clientCount; i++) {
            String userId = "userId" + i;
            threads[i] = new Thread(() -> {
                new Client(userId, finalInterval, finalAddress).start();
            });
            threads[i].start();
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

        private static final String[] ROOM_IDS = new String[2];
        static {
            for(int i = 0; i < ROOM_IDS.length; i++) {
                ROOM_IDS[i] = UUID.randomUUID().toString();
            }
        }

        private final String userId;
        private final long interval;
        private final String address;

        public Client(String userId, long interval, String address) {
            this.userId = userId;
            this.interval = interval;
            this.address = address;
        }

        public void start() {
            Socket socket;
            try {
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

                MessageUtils.register(out, userId);
                out.flush();

                while(true) {
                    String roomId = ROOM_IDS[random.nextInt(ROOM_IDS.length)];
                    int level = random.nextInt(1, 100);
                    String nickname = UUID.randomUUID().toString();
                    writeRandomMessage(out, roomId, nickname, level);

                    Thread.sleep(random.nextLong(interval));
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
