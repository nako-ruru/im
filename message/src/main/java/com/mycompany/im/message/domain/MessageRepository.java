package com.mycompany.im.message.domain;

import java.util.List;

/**
 * Created by Administrator on 2017/5/13.
 */
public interface MessageRepository {

    List<Message> findByRoomIdAndGreaterThan(String roomId, long from);

}
