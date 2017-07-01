package com.mycompany.im.connector;

import com.google.common.collect.ImmutableMap;
import com.google.gson.Gson;

import java.io.DataOutput;
import java.io.IOException;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
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
        Map<String, Object> params = ImmutableMap.of(
                "UserId", userId,
                "Pass", ""
        );
        writeMsg(out, params, 0);
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
        Map<String, Object> params = ImmutableMap.of(
                "roomId", roomId,
                "content", content,
                "nickname", nickname,
                "level", level
        );
        writeMsg(out, params, 1);
    }

    /**
     * 点赞
     * @param out
     * @param roomId
     * @param level
     * @throws IOException
     */
    public static void support(DataOutput out, String roomId, String nickname, int level) throws IOException {
        Map<String, Object> params = ImmutableMap.of(
                "roomId", roomId,
                "nickname", nickname,
                "level", level
        );
        writeMsg(out, params, 2);
    }

    /**
     * 送礼物
     * @param out
     * @param roomId
     * @param giftId
     * @param level
     * @throws IOException
     */
    public static void sendGift(DataOutput out, String roomId, String giftId, String nickname, int level) throws IOException {
        Map<String, Object> params = ImmutableMap.of(
                "roomId", roomId,
                "nickname", nickname,
                "level", level,
                "giftId", giftId
        );
        writeMsg(out, params, 3);
    }

    /**
     * 送礼物
     * @param out
     * @param roomId
     * @param level
     * @throws IOException
     */
    public static void enterRoom(DataOutput out, String roomId, String nickname, int level) throws IOException {
        Map<String, Object> params = ImmutableMap.of(
                "roomId", roomId,
                "nickname", nickname,
                "level", level
        );
        writeMsg(out, params, 4);
    }

    /**
     * 分享
     * @param out
     * @param roomId
     * @param level
     * @throws IOException
     */
    public static void share(DataOutput out, String roomId, String nickname, int level) throws IOException {
        Map<String, Object> params = ImmutableMap.of(
                "roomId", roomId,
                "nickname", nickname,
                "level", level
        );
        writeMsg(out, params, 5);
    }

    /**
     * 升级
     * @param out
     * @param roomId
     * @param level
     * @throws IOException
     */
    public static void levelUp(DataOutput out, String roomId, String nickname, int level) throws IOException {
        Map<String, Object> params = ImmutableMap.of(
                "roomId", roomId,
                "nickname", nickname,
                "level", level
        );
        writeMsg(out, params, 6);
    }

    private static void writeMsg(DataOutput out, Object o, int type) throws IOException {
        String json = new Gson().toJson(o);
        byte[] bytes = json.getBytes(StandardCharsets.UTF_8);
        out.writeInt(4 + bytes.length);
        out.writeInt(type);
        out.write(bytes);
    }

}
