package com.mycompany.im.compute.domain;

import java.util.Map;

/**
 * Created by Administrator on 2017/5/28.
 */
public class FromConnectorMessage {

    public final String messageId;
    public final String roomId, userId, nickname;
    public final long time;
    public final int level, type;
    public final Map<String, Object> params;

    private FromConnectorMessage() {
        this(null, null, null, null, 0, 0, null);
    }

    public FromConnectorMessage(String messageId, String roomId, String userId, String nickname, int level, int type, Map<String, Object> params) {
        this.messageId = messageId;
        this.roomId = roomId;
        this.userId = userId;
        this.nickname = nickname;
        this.params = params;
        this.type = type;
        this.level = level;
        this.time = System.currentTimeMillis();
    }

}
