package com.mycompany.im.connector;

import redis.clients.jedis.ShardedJedisPool;

public class PublicJedisPoolUtils extends JedisPoolUtils {

    public static ShardedJedisPool pool() {
        return JedisPoolUtils.pool();
    }

}
