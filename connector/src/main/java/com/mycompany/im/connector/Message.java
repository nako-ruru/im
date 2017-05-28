package com.mycompany.im.connector;

/**
 * Created by Administrator on 2017/5/28.
 */
class Message {

    public final String userId, content;
    public final long time;

    public Message(String userId, String content) {
        this.userId = userId;
        this.content = content;
        time = System.currentTimeMillis();
    }


}
