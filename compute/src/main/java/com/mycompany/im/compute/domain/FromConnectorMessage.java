package com.mycompany.im.compute.domain;

import com.google.gson.Gson;
import java.text.SimpleDateFormat;
import java.util.Date;

import java.util.Map;

/**
 * Created by Administrator on 2017/5/28.
 */
public class FromConnectorMessage {

    public final String messageId;
    public final String roomId, userId, nickname;
    public final long time;
    public final String timeText;
    public final int level, type;
    public final Map<String, String> params;

    private FromConnectorMessage() {
        this(null, null, null, null, 0, 0, null, 0);
    }

    public FromConnectorMessage(String messageId, String roomId, String userId, String nickname, int level, int type, Map<String, String> params, long time) {
        this.messageId = messageId;
        this.roomId = roomId;
        this.userId = userId;
        this.nickname = nickname;
        this.params = params;
        this.type = type;
        this.level = level;
        this.time = time;
        this.timeText = new SimpleDateFormat("MM-dd HH:mm:ss").format(new Date(time));
    }

    @Override
    public String toString() {
        return new Gson().toJson(this);
    }
}
