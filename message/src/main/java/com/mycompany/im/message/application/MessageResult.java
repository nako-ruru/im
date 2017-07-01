package com.mycompany.im.message.application;

import com.google.gson.Gson;

import java.util.Map;

/**
 * Created by Administrator on 2017/5/28.
 */
public class MessageResult {

    private final String roomId;
    private final String userId;
    private final long time;
    private final int type, level;
    private final String nickname;
    private final Map<String, Object> params;

    public MessageResult(String roomId, String userId, int type, Map<String, Object> params, String nickname, int level, long time) {
        this.roomId = roomId;
        this.userId = userId;
        this.params = params;
        this.time = time;
        this.type = type;
        this.level = level;
        this.nickname = nickname;
    }

    public String getRoomId() {
        return roomId;
    }

    public String getUserId() {
        return userId;
    }

    public long getTime() {
        return time;
    }

    public int getType() {
        return type;
    }

    public Map<String, Object> getParams() {
        return params;
    }

    public int getLevel() {
        return level;
    }

    public String getNickname() {
        return nickname;
    }

    @Override
    public String toString() {
        return new Gson().toJson(this);
    }

}
