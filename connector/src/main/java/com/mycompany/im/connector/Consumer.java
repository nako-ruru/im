package com.mycompany.im.connector;

import com.google.gson.Gson;
import redis.clients.jedis.*;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.BlockingQueue;

/**
 * Created by Administrator on 2017/5/28.
 */
class Consumer implements Runnable {

    private static final int MAX_ELEMENTS = 100000;

    private final BlockingQueue<Message> messages;

    public Consumer(BlockingQueue<Message> messages) {
        this.messages = messages;
    }

    @Override
    public void run() {
        List<JedisShardInfo> shards = JedisPoolUtils.read();
        ShardedJedisPool pool = new ShardedJedisPool(new JedisPoolConfig(), shards);

        while(true) {
            try {
                Message first = messages.take();
                Collection<Message> ready = new LinkedList<>();
                ready.add(first);
                messages.drainTo(ready, MAX_ELEMENTS - 1);
                try (ShardedJedis jedis = pool.getResource()) {
                    ShardedJedisPipeline pipelined = jedis.pipelined();
                    for(Message message : ready) {
                        pipelined.rpush(message.roomId, json(message));
                    }
                    pipelined.sync();
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

    }

    private static String json(Message c) {
        return new Gson().toJson(c);
    }


}
