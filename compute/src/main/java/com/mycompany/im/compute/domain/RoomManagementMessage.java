package com.mycompany.im.compute.domain;

/**
 * Created by Administrator on 2017/7/10.
 */
public class RoomManagementMessage {

    private String type;
    private Payload payload;

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Payload getPayload() {
        return payload;
    }

    public void setPayload(Payload payload) {
        this.payload = payload;
    }

}
