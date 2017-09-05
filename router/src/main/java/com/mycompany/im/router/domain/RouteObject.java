package com.mycompany.im.router.domain;

/**
 * Created by Administrator on 2017/9/2.
 */
public class RouteObject {

    private Payload payload;
    private int importance;

    public Payload getPayload() {
        return payload;
    }

    public void setPayload(Payload payload) {
        this.payload = payload;
    }

    public int getImportance() {
        return importance;
    }

    public void setImportance(int importance) {
        this.importance = importance;
    }

}
