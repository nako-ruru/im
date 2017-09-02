package com.mycompany.im.router.domain;

/**
 * Created by Administrator on 2017/9/2.
 */
public class RouteObject {

    private Payload payload;
    private int rank;

    public Payload getPayload() {
        return payload;
    }

    public void setPayload(Payload payload) {
        this.payload = payload;
    }

    public int getRank() {
        return rank;
    }

    public void setRank(int rank) {
        this.rank = rank;
    }

}
