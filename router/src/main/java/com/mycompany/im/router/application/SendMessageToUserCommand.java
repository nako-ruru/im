package com.mycompany.im.router.application;

import com.google.gson.Gson;

/**
 * Created by Administrator on 2017/9/3.
 */
public class SendMessageToUserCommand {

    private String toUserId;
    private String content;
    private int rank;

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

    public int getRank() {
        return rank;
    }

    public void setRank(int rank) {
        this.rank = rank;
    }

    @Override
    public String toString() {
        return new Gson().toJson(this);
    }


}
