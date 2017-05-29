package com.mycompany.im.connector;

/**
 * Created by Administrator on 2017/5/28.
 */
class Message {

    public final String roomId, userId, content;
    public final long time;

    public Message(String roomId, String userId, String content) {
        this.roomId = roomId;
        this.userId = userId;
        this.content = content;
        time = System.currentTimeMillis();
    }


}
