package com.mycompany.im.compute.domain;

import java.util.Collection;

/**
 * Created by Administrator on 2017/9/3.
 */
public interface MessageRepository {
    void save(Collection<ToPollingMessage> message);
}
