package com.mycompany.im.compute.domain;

import java.util.Map;

/**
 * Created by Administrator on 2017/9/3.
 */
public class ToPollingMessage {

    public final String messageId;
    public final String toRoomId, fromUserId, fromNickname;
    public final long time;
    public final int fromLevel, type;
    public final Map<String, String> params;

    public ToPollingMessage(String messageId, String toRoomId, String fromUserId, String fromNickname, int fromLevel, int type, Map<String, String> params) {
        this.messageId = messageId;
        this.toRoomId = toRoomId;
        this.fromUserId = fromUserId;
        this.fromNickname = fromNickname;
        this.params = params;
        this.type = type;
        this.fromLevel = fromLevel;
        this.time = System.currentTimeMillis();
    }

}
