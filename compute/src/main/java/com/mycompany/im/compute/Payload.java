package com.mycompany.im.compute;

/**
 * Created by Administrator on 2017/7/12.
 */
public class Payload {

    private String roomId, userId;
    private boolean add;

    public String getRoomId() {
        return roomId;
    }

    public void setRoomId(String roomId) {
        this.roomId = roomId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public boolean isAdd() {
        return add;
    }

    public void setAdd(boolean add) {
        this.add = add;
    }

}
