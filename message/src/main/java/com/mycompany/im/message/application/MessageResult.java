package com.mycompany.im.message.application;

import com.google.gson.Gson;

/**
 * Created by Administrator on 2017/5/28.
 */
public class MessageResult {

    private final String roomId, userId, content;
    private final long time;
    private final int type, level;

    public MessageResult(String roomId, String userId, String content, int type, int level, long time) {
        this.roomId = roomId;
        this.userId = userId;
        this.content = content;
        this.time = time;
        this.type = type;
        this.level = level;
    }

    public String getRoomId() {
        return roomId;
    }

    public String getUserId() {
        return userId;
    }

    public String getContent() {
        return content;
    }

    public long getTime() {
        return time;
    }

    public int getType() {
        return type;
    }

    public int getLevel() {
        return level;
    }

    @Override
    public String toString() {
        return new Gson().toJson(this);
    }

}
