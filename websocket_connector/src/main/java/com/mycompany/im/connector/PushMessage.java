package com.mycompany.im.connector;

import com.google.gson.Gson;

/**
 * Created by Administrator on 2017/7/8.
 */
public class PushMessage {

    private String userId;
    private String moduleId;
    private String content;

    private PushMessage() {
    }

    public String getUserId() {
        return userId;
    }

    public String getModuleId() {
        return moduleId;
    }

    public String getContent() {
        return content;
    }

    @Override
    public String toString() {
        return new Gson().toJson(this);
    }
}
