package com.mycompany.im.connector;

import java.io.DataInputStream;
import java.io.DataOutput;
import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

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
     * @see Socket#getInputStream()
     * @see DataInputStream#DataInputStream(InputStream)
     */
    public static void register(DataOutput out, String userId) throws IOException {
        writeCharSequence(out, userId);
    }

    /**
     * 普通聊天
     * @param out
     * @param roomId
     * @param content
     * @param level
     * @throws IOException
     */
    public static void chat(DataOutput out, String roomId, String content, int level) throws IOException {
        writeCharSequence(out, roomId);
        out.writeInt(level);
        out.writeInt(1);
        writeCharSequence(out, content);
    }

    /**
     * 点赞
     * @param out
     * @param roomId
     * @param level
     * @throws IOException
     */
    public static void support(DataOutput out, String roomId, int level) throws IOException {
        writeCharSequence(out, roomId);
        out.writeInt(level);
        out.writeInt(2);
    }

    /**
     * 发送礼物
     * @param out
     * @param roomId
     * @param giftId
     * @param level
     * @throws IOException
     */
    public static void sendGift(DataOutput out, String roomId, String giftId, int level) throws IOException {
        writeCharSequence(out, roomId);
        out.writeInt(level);
        out.writeInt(3);
        writeCharSequence(out, giftId);
    }

    /**
     * 分享
     * @param out
     * @param roomId
     * @param level
     * @throws IOException
     */
    public static void share(DataOutput out, String roomId, int level) throws IOException {
        writeCharSequence(out, roomId);
        out.writeInt(level);
        out.writeInt(5);
    }

    /**
     * 升级
     * @param out
     * @param roomId
     * @param level
     * @throws IOException
     */
    public static void levelUp(DataOutput out, String roomId, int level) throws IOException {
        writeCharSequence(out, roomId);
        out.writeInt(level);
        out.writeInt(6);
    }

    private static void writeCharSequence(DataOutput out, String chars) throws IOException {
        byte[] bytes = chars.getBytes(StandardCharsets.UTF_8);
        out.writeInt(bytes.length);
        out.write(bytes);
    }

}
