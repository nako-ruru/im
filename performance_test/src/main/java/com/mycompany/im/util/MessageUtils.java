package com.mycompany.im.util;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

import java.io.*;
import java.net.Socket;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.DataFormatException;
import java.util.zip.Inflater;

/**
 * Created by Administrator on 2017/6/5.
 */
public class MessageUtils {

    static final Charset UTF_8 = Charset.forName("UTF-8");

    /**
     * 对当前{@link java.net.Socket}连接注册userId。该方法在每次连接后调用且仅调用一次
     * @param out
     * @param userId
     * @param clientVersion 
     * @throws IOException
     *
     * @see Socket#getOutputStream()
     * @see java.io.DataOutputStream#DataOutputStream(java.io.OutputStream)
     */
    public static void register(DataOutput out, String userId, String clientVersion) throws IOException {
        Map<String, Object> params = map(
                "userId", userId,
                "version", clientVersion
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
                        int contentLength = length - 4;
                        byte[] bytes = new byte[contentLength];
                        int read = 0;
                        while((read += in.read(bytes, read, contentLength - read)) < contentLength) {
                        }
                        try {
                            handle(bytes, 0, contentLength, type, consumer);
                        } catch (Exception e) {
                            if (eConsumer != null) {
                                eConsumer.accept(e);
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
        private String messageId;
        private long time;
        private String timeText;
        @Deprecated
        private String userId, roomId, content;
        private String toUserId, toRoomId;
        private Map<String, Object> params;

        public String getMessageId() {
            return messageId;
        }

        public void setMessageId(String messageId) {
            this.messageId = messageId;
        }

        public long getTime() {
            return time;
        }

        public void setTime(long time) {
            this.time = time;
        }

        public String getTimeText() {
            return timeText;
        }

        public void setTimeText(String timeText) {
            this.timeText = timeText;
        }

        
        
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

    static <K, V> Map<K, V> map(Object... keyValues) {
        Map map = new HashMap<>();
        for(int i = 0; i < keyValues.length / 2; i++) {
            map.put(keyValues[i * 2], keyValues[i * 2 + 1]);
        }
        return map;
    }

    private static String json(Object o) {
        return new Gson().toJson(o);
    }
    
    static void handle(byte[] bytes, int offset, int contentLength, int type, Consumer<Msg> consumer) throws IOException, DataFormatException {
        switch (type) {
            case 30000:
                handle30000(bytes, 0, contentLength, type, consumer);
                break;
            case 30001:
                try(DataInputStream din = new DataInputStream(new ByteArrayInputStream(bytes, offset, contentLength))) {
                    boolean compressed = din.readBoolean();
                    byte[] contentBytes = new byte[bytes.length - 1];
                    int read = din.read(contentBytes);
                    if(compressed) {
                        byte[] uncopmpressed = decompress(contentBytes, 0, read);
                        handle30000(uncopmpressed, 0, uncopmpressed.length, type, consumer);
                    } else{
                        handle30000(contentBytes, 0, read, type, consumer);
                    }
                }  
                break;
            default:
                throw new RuntimeException("unknown type: " + type);
        }
    }
    
    private static void handle30000(byte[] bytes, int offset, int contentLength, int type, Consumer<Msg> consumer) {
        String jsonText = new String(bytes, offset, contentLength, UTF_8);
        if(jsonText.matches("\\s*\\[.+")) {
            try {
                Msg[] msgs = new Gson().fromJson(jsonText, Msg[].class);
                if (consumer != null) {
                    for(Msg msg : msgs) {
                        consumer.accept(msg);
                    }
                }
            } catch(JsonSyntaxException e) {
                throw new RuntimeException(jsonText, e);
            }
        } else if(jsonText.matches("\\s*\\{.+")) {
            try {
                Msg msg = new Gson().fromJson(jsonText, Msg.class);
                if (consumer != null) {
                    consumer.accept(msg);
                }
            } catch(JsonSyntaxException e) {
                throw new RuntimeException(jsonText, e);
            }
        }
    }
    
    private static byte[] decompress(byte[] data, int offset, int length) throws DataFormatException {
        Inflater inflater = new Inflater();
        ByteArrayOutputStream out = new ByteArrayOutputStream(data.length);
        try {
            inflater.setInput(data, offset, length);
            byte[] buffer = new byte[1024];
            while (!inflater.finished()) {
                int count = inflater.inflate(buffer);
                out.write(buffer, 0, count);
            }
            byte[] output = out.toByteArray();
            return output;
        } finally {
            inflater.end();
            try {
                out.close();
            } catch(IOException e) {
                throw new Error("panic");
            }
        }
    }

}
