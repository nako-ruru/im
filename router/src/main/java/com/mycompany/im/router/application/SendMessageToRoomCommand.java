/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mycompany.im.router.application;

import com.google.gson.Gson;

/**
 *
 * @author Administrator
 */
public class SendMessageToRoomCommand {

    private String toRoomId;
    private String content;
    private int rank;

    public String getToRoomId() {
        return toRoomId;
    }

    public void setToRoomId(String toRoomId) {
        this.toRoomId = toRoomId;
    }

    @Deprecated
    public String getRoomId() {
        return toRoomId;
    }

    @Deprecated
    public void setRoomId(String roomId) {
        this.toRoomId = roomId;
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
