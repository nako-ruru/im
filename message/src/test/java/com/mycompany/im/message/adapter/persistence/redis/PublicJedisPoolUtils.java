package com.mycompany.im.message.adapter.persistence.redis;

import redis.clients.jedis.ShardedJedisPool;

public class PublicJedisPoolUtils extends JedisPoolUtils {

    public static ShardedJedisPool pool() {
        return JedisPoolUtils.pool();
    }

}
