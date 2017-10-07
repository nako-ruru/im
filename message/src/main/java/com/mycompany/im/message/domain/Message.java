package com.mycompany.im.message.domain;

import com.google.gson.Gson;

import java.util.Map;

/**
 * Created by Administrator on 2017/5/28.
 */
public class Message {

    private String messageId;
    private String toRoomId;
    private String fromUserId;
    private String fromNickname;
    private long time;
    private int type, fromLevel;
    private Map<String, String> params;

    public String getMessageId() {
        return messageId;
    }

    public void setMessageId(String messageId) {
        this.messageId = messageId;
    }

    public String getToRoomId() {
        return toRoomId;
    }

    public void setToRoomId(String toRoomId) {
        this.toRoomId = toRoomId;
    }

    public String getFromUserId() {
        return fromUserId;
    }

    public void setFromUserId(String fromUserId) {
        this.fromUserId = fromUserId;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public String getFromNickname() {
        return fromNickname;
    }

    public void setFromNickname(String fromNickname) {
        this.fromNickname = fromNickname;
    }

    public Map<String, String> getParams() {
        return params;
    }

    public void setParams(Map<String, String> params) {
        this.params = params;
    }

    public int getFromLevel() {
        return fromLevel;
    }

    public void setFromLevel(int fromLevel) {
        this.fromLevel = fromLevel;
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
