package com.mycompany.im.message.application;

import com.google.gson.Gson;

/**
 * Created by Administrator on 2017/5/28.
 */
public class MessageResult {

    private final String roomId, userId, content;
    private final long time;

    public MessageResult(String roomId, String userId, String content, long time) {
        this.roomId = roomId;
        this.userId = userId;
        this.content = content;
        this.time = time;
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

    @Override
    public String toString() {
        return new Gson().toJson(this);
    }

}
