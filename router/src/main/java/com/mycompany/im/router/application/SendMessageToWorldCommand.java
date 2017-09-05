package com.mycompany.im.router.application;

import com.google.gson.Gson;

/**
 * Created by Administrator on 2017/6/27.
 */
public class SendMessageToWorldCommand {

    private String content;
    private int importance;

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
