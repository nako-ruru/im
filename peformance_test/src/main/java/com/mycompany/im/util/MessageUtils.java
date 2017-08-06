package com.mycompany.im.util;

import com.google.gson.Gson;

import java.io.DataInputStream;
import java.io.DataOutput;
import java.io.IOException;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Administrator on 2017/6/5.
 */
public class MessageUtils {

    /**
     * 对当前{@link java.net.Socket}连接注册userId。该方法在每次连接后调用且仅调用一次
     * @param out
     * @param userId
     * @throws IOException
     *
     * @see Socket#getOutputStream()
     * @see java.io.DataOutputStream#DataOutputStream(java.io.OutputStream)
     */
    public static void register(DataOutput out, String userId) throws IOException {
        Map<String, Object> params = map(
                "UserId", userId,
                "Pass", ""
        );
        writeMsg(out, params, 0);
    }

    /**
     * 为{@link java.net.Socket}连接的{@link DataInputStream}注册推送消息回调
     * @param in
     * @param consumer
     * @param eConsumer
     */
    public static void pushCallback(DataInputStream in, Consumer<Msg> consumer, Consumer<IOException> eConsumer){
        Thread t = new Thread(() -> {
            try {
                while(true) {
                    int length = in.readInt();
                    int type = in.readInt();
                    if(type == 30000) {
                        byte[] bytes = new byte[length - 4];
                        in.read(bytes);
                        String jsonText = new String(bytes, StandardCharsets.UTF_8);
                        Msg msg = new Gson().fromJson(jsonText, Msg.class);
                        if(consumer != null) {
                            consumer.accept(msg);
                        }
                    }
                }
            }
            catch (IOException e) {
                if(eConsumer != null) {
                    eConsumer.accept(e);
                }
            }
        });
        t.start();
    }

    /**
     * 普通聊天
     * @param out
     * @param roomId
     * @param content
     * @param nickname
     *@param level  @throws IOException
     */
    public static void chat(DataOutput out, String roomId, String content, String nickname, int level) throws IOException {
        Map<String, Object> params = map(
                "roomId", roomId,
                "content", content,
                "nickname", nickname,
                "level", level
        );
        writeMsg(out, params, 1);
    }

    private static void writeMsg(DataOutput out, Object o, int type) throws IOException {
        String json = json(o);
        byte[] bytes = json.getBytes(StandardCharsets.UTF_8);
        out.writeInt(4 + bytes.length);
        out.writeInt(type);
        out.write(bytes);
    }

    public static class Msg {
        private String userId, moduleId, content;

        public String getUserId() {
            return userId;
        }
        public void setUserId(String userId) {
            this.userId = userId;
        }

        public String getModuleId() {
            return moduleId;
        }
        public void setModuleId(String moduleId) {
            this.moduleId = moduleId;
        }

        public String getContent() {
            return content;
        }
        public void setContent(String content) {
            this.content = content;
        }

        @Override
        public String toString() {
            return json(this);
        }

    }

    public interface Consumer<T> {
        void accept(T t);
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
