package com.mycompany.im.message.domain;

import com.google.gson.Gson;

/**
 * Created by Administrator on 2017/5/28.
 */
public class Message {

    private String roomId, userId, content;
    private long time;

    public String getRoomId() {
        return roomId;
    }

    public void setRoomId(String roomId) {
        this.roomId = roomId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }

    @Override
    public String toString() {
        return new Gson().toJson(this);
    }

}
