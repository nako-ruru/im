package com.mycompany.im.connector;

import java.io.DataOutput;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 * Created by Administrator on 2017/6/5.
 */
public class MessageUtils {

    public static void register(DataOutput out, String userId) throws IOException {
        writeCharSequence(out, userId);
    }

    public static void chat(DataOutput out, String roomId, String content, int level) throws IOException {
        writeCharSequence(out, roomId);
        out.writeInt(level);
        out.writeInt(1);
        writeCharSequence(out, content);
    }

    public static void support(DataOutput out, String roomId, int level) throws IOException {
        writeCharSequence(out, roomId);
        out.writeInt(level);
        out.writeInt(2);
    }

    public static void sendGift(DataOutput out, String roomId, String giftId, int level) throws IOException {
        writeCharSequence(out, roomId);
        out.writeInt(level);
        out.writeInt(3);
        writeCharSequence(out, giftId);
    }

    public static void share(DataOutput out, String roomId, int level) throws IOException {
        writeCharSequence(out, roomId);
        out.writeInt(level);
        out.writeInt(5);
    }

    public static void levelUp(DataOutput out, String roomId, int level) throws IOException {
        writeCharSequence(out, roomId);
        out.writeInt(level);
        out.writeInt(6);
    }

    public static void writeCharSequence(DataOutput out, String chars) throws IOException {
        byte[] bytes = chars.getBytes(StandardCharsets.UTF_8);
        out.writeInt(bytes.length);
        out.write(bytes);
    }

}
