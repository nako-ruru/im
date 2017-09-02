package com.mycompany.im.message.application;

import com.google.gson.Gson;

import java.util.Map;

/**
 * Created by Administrator on 2017/5/28.
 */
public class MessageResult {

    private final String toRoomId;
    private final String fromUserId, fromNickname;
    private final int fromLevel;
    private final int type;
    private final Map<String, Object> params;
    private final long time;
    
    @Deprecated
    private final String roomId;
    @Deprecated
    private final String userId, nickname;
    @Deprecated
    private final int level;

    public MessageResult(String toRoomId, String fromUserId, int type, Map<String, Object> params, String fromNickname, int fromLevel, long time) {
        this.toRoomId = toRoomId;
        this.fromUserId = fromUserId;
        this.fromLevel = fromLevel;
        this.fromNickname = fromNickname;
        this.type = type;
        this.params = params;
        this.time = time;
        
        this.roomId = toRoomId;
        this.userId = fromUserId;
        this.nickname = fromNickname;
        this.level = fromLevel;
    }

    public String getToRoomId() {
        return toRoomId;
    }

    public String getFromUserId() {
        return fromUserId;
    }

    public String getFromNickname() {
        return fromNickname;
    }

    public long getTime() {
        return time;
    }

    public int getFromLevel() {
        return fromLevel;
    }

    public int getType() {
        return type;
    }

    public Map<String, Object> getParams() {
        return params;
    }

    @Deprecated
    public String getRoomId() {
        return roomId;
    }

    @Deprecated
    public String getUserId() {
        return userId;
    }

    @Deprecated
    public String getNickname() {
        return nickname;
    }

    @Deprecated
    public int getLevel() {
        return level;
    }

    @Override
    public String toString() {
        return new Gson().toJson(this);
    }

}
