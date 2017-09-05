package com.mycompany.im.connector;

import com.google.gson.Gson;

import java.io.*;
import java.net.Socket;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Administrator on 2017/6/5.
 */
public class MessageUtils {

    private static final Charset UTF_8 = Charset.forName("UTF-8");

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
                "userId", userId
        );
        writeMsg(out, params, 0);
    }

    /**
     * 进入房间。
     * @param out
     * @param roomId
     * @throws IOException
     */
    public static void enter(DataOutput out, String roomId) throws IOException {
        Map<String, Object> params = map(
                "roomId", roomId
                );
        writeMsg(out, params, 4);
    }

    /**
     * 为{@link java.net.Socket}连接的{@link DataInputStream}注册推送消息回调
     * @param in
     * @param consumer
     * @param eConsumer
     */
    public static void pushCallback(DataInputStream in, Consumer<Msg> consumer, Consumer<Exception> eConsumer){
        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    while (true) {
                        int length = in.readInt();
                        int type = in.readInt();
                        if (type == 30000) {
                            int contentLength = length - 4;
                            byte[] bytes = new byte[contentLength];
                            int read = 0;
                            while((read += in.read(bytes, read, contentLength - read)) < contentLength) {
                            }
                            String jsonText = new String(bytes, 0, contentLength, UTF_8);
                            try {
                                Msg msg = new Gson().fromJson(jsonText, Msg.class);
                                if (consumer != null) {
                                    consumer.accept(msg);
                                }
                            } catch (Exception e) {
                                if (eConsumer != null) {
                                    eConsumer.accept(e);
                                }
                            }
                        }
                    }
                } catch (IOException e) {
                    if (eConsumer != null) {
                        eConsumer.accept(e);
                    }
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
     * @param level
     * @throws IOException
     */
    public static void chat(DataOutput out, @Deprecated String roomId, String content, String nickname, int level) throws IOException {
        Map<String, Object> params = map(
                "roomId", roomId,
                "content", content,
                "nickname", nickname,
                "level", level
        );
        writeMsg(out, params, 1);
    }

    static byte[] createMsg(Object o, int type) throws IOException {
        String json = json(o);
        byte[] bytes = json.getBytes(UTF_8);
        ByteArrayOutputStream baos = new ByteArrayOutputStream(4 + 4 + bytes.length);
        DataOutputStream out = new DataOutputStream(baos);
        out.writeInt(4 + bytes.length);
        out.writeInt(type);
        out.write(bytes);
        return baos.toByteArray();
    }

    private static void writeMsg(DataOutput out, Object o, int type) throws IOException {
        out.write(createMsg(o, type));
    }

    public static class Msg {
        @Deprecated
        private String userId, roomId, content;
        private String toUserId, toRoomId;
        private Map<String, Object> params;

        public String getUserId() {
            return userId;
        }
        public void setUserId(String userId) {
            this.userId = userId;
        }

        public String getRoomId() {
            return roomId;
        }
        public void setRoomId(String roomId) {
            this.roomId = roomId;
        }

        public String getContent() {
            return content;
        }
        public void setContent(String content) {
            this.content = content;
        }

        public String getToUserId() {
            return toUserId;
        }

        public void setToUserId(String toUserId) {
            this.toUserId = toUserId;
        }

        public String getToRoomId() {
            return toRoomId;
        }

        public void setToRoomId(String toRoomId) {
            this.toRoomId = toRoomId;
        }

        public Map<String, Object> getParams() {
            return params;
        }

        public void setParams(Map<String, Object> params) {
            this.params = params;
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
