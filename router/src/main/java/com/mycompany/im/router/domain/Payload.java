package com.mycompany.im.router.domain;

import java.util.Map;

/**
 * Created by Administrator on 2017/5/28.
 */
public class Payload {

    public final String messageId;
    public final String toRoomId, toUserId;
    public final long time;
    public final int type;
    public final Map<String, Object> params;

    public Payload(String messageId, String toRoomId, String toUserId, int type, Map<String, Object> params) {
        this.messageId = messageId;
        this.toRoomId = toRoomId;
        this.toUserId = toUserId;
        this.params = params;
        this.type = type;
        time = System.currentTimeMillis();
    }

}
