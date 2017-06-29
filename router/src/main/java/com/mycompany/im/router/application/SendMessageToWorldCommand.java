package com.mycompany.im.router.application;

import com.google.gson.Gson;

/**
 * Created by Administrator on 2017/6/27.
 */
public class SendMessageToWorldCommand {

    private String content;
    private String moduleId;

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getModuleId() {
        return moduleId;
    }

    public void setModuleId(String moduleId) {
        this.moduleId = moduleId;
    }

    @Override
    public String toString() {
        return new Gson().toJson(this);
    }

}
