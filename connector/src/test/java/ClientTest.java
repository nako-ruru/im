import com.mycompany.im.connector.MessageUtils;
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
                MessageUtils.register(out, userId);

                while(true) {
                    String roomId = ROOM_IDS[random.nextInt(ROOM_IDS.length)];
                    int level = random.nextInt(1, 100);
                    writeRandomMessage(out, roomId, level);

                    Thread.sleep(random.nextLong(3000L));
                }
            } catch (IOException | InterruptedException e) {
                e.printStackTrace();
            }
        }

        private static void writeRandomMessage(DataOutput out, String roomId, int level) throws IOException {
            ThreadLocalRandom random = ThreadLocalRandom.current();
            int[] availableTypes = {1, 2, 3, 5, 6};
            int type = availableTypes[random.nextInt(availableTypes.length)];
            switch (type) {
                case 1:
                    String content = WORDS[random.nextInt(WORDS.length)];
                    MessageUtils.chat(out, roomId, content, level);
                    break;
                case 2:
                    MessageUtils.support(out, roomId, level);
                    break;
                case 3:
                    String giftId = UUID.randomUUID().toString();
                    MessageUtils.sendGift(out, roomId, giftId, level);
                    break;
                case 5:
                    MessageUtils.share(out, roomId, level);
                    break;
                case 6:
                    MessageUtils.levelUp(out, roomId, level);
                    break;
                default:
                    break;
            }
        }
    }

}
