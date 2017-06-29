package com.mycompany.im.router.adapter.persistence.redis;

import com.mycompany.im.router.domain.RoomRepository;
import com.mycompany.im.router.domain.UserConnection;
import org.springframework.stereotype.Component;

import java.util.Collection;

/**
 * Created by Administrator on 2017/6/26.
 */
@Component
public class RedisRoomRepository implements RoomRepository {
    @Override
    public Collection<UserConnection> findUserConnectionsByRoomId(String roomId) {
        return null;
    }
}
