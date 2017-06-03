package com.mycompany.im.connector;

/**
 * Created by Administrator on 2017/5/28.
 */
class Message {

    public final String roomId, userId, content;
    public final long time;
    public final int level, type;

    public Message(String roomId, String userId, String content, int type, int level) {
        this.roomId = roomId;
        this.userId = userId;
        this.content = content;
        this.type = type;
        this.level = level;
        time = System.currentTimeMillis();
    }


}
