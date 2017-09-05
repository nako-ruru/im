package com.mycompany.im.router.application;

import com.google.gson.Gson;

/**
 * Created by Administrator on 2017/9/3.
 */
public class SendMessageToUserCommand {

    private String toUserId;
    private String content;
    private int importance;

    public String getToUserId() {
        return toUserId;
    }

    public void setToUserId(String toUserId) {
        this.toUserId = toUserId;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public int getImportance() {
        return importance;
    }

    public void setImportance(int importance) {
        this.importance = importance;
    }

    @Override
    public String toString() {
        return new Gson().toJson(this);
    }


}
