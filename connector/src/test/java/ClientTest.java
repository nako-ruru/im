import org.junit.Test;

import java.io.DataOutput;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Created by Administrator on 2017/5/28.
 */
public class ClientTest {

    @Test
    public void testA() throws InterruptedException {
        int clientCount = 2;
        Thread[] threads = new Thread[clientCount];
        for(int i = 0; i < clientCount; i++) {
            int finalI = i;
            threads[i] = new Thread(() -> {
                new Client("userId" + finalI).start();
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

        private static final String[] ROOM_IDS = new String[3];
        static {
            for(int i = 0; i < ROOM_IDS.length; i++) {
                ROOM_IDS[i] = UUID.randomUUID().toString();
            }
        }

        private final String userId;

        public Client(String userId) {
            this.userId = userId;
        }

        public void start() {
            Socket socket;
            try {
                String host = "localhost";
                //String host = "47.92.98.23";
                socket = new Socket(host, 6060);

                ThreadLocalRandom random = ThreadLocalRandom.current();

                DataOutputStream out = new DataOutputStream(socket.getOutputStream());
                writeCharSequence(out, userId);

                while(true) {
                    writeCharSequence(out, ROOM_IDS[random.nextInt(ROOM_IDS.length)]);
                    out.writeInt(random.nextInt(1, 100));
                    writeRandomType(out);
                    Thread.sleep(random.nextLong(3000L));
                }
            } catch (IOException | InterruptedException e) {
                e.printStackTrace();
            }
        }

        private static void writeCharSequence(DataOutput out, String chars) throws IOException {
            byte[] bytes = chars.getBytes(StandardCharsets.UTF_8);
            out.writeInt(bytes.length);
            out.write(bytes);
        }

        private static void writeRandomType(DataOutput out) throws IOException {
            ThreadLocalRandom random = ThreadLocalRandom.current();
            int[] availableTypes = {1, 2, 3, 5, 6};
            int type = availableTypes[random.nextInt(availableTypes.length)];
            out.writeInt(type);
            switch (type) {
                case 1:
                    writeCharSequence(out, WORDS[random.nextInt(WORDS.length)]);
                    break;
                case 3:
                    writeCharSequence(out, UUID.randomUUID().toString());
                    break;
                default:
                    break;
            }
        }

    }

}
