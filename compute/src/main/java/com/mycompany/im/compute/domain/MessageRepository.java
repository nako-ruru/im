package com.mycompany.im.compute.domain;

/**
 * Created by Administrator on 2017/9/3.
 */
public interface MessageRepository {
    void save(ToPollingMessage message);
}
