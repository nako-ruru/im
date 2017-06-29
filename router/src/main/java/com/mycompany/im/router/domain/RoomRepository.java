package com.mycompany.im.router.domain;

import java.util.Collection;

/**
 * Created by Administrator on 2017/6/26.
 */
public interface RoomRepository {

    Collection<UserConnection> findUserConnectionsByRoomId(String roomId);

}
