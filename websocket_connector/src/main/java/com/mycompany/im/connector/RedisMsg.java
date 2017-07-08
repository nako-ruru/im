package com.mycompany.im.connector;

import java.util.Map;

/**
 * Created by Administrator on 2017/7/8.
 */
public class RedisMsg {

    private String roomId;
    private String userId;
    private long time;
    private int type;
    private String nickname;
    private int level;
    private Map<String, Object> params;

    public RedisMsg(String roomId, String userId, String nickname, int level, int type, Map<String, Object> params) {
        this.roomId = roomId;
        this.userId = userId;
        this.nickname = nickname;
        this.params = params;
        this.type = type;
        this.level = level;
        time = System.currentTimeMillis();
    }

}
