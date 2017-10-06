/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mycompany.im.framework.spring;

import com.google.gson.Gson;
import org.springframework.data.redis.connection.RedisSentinelConfiguration;

/**
 *
 * @author Administrator
 */
public class MyRedisSentinelConfiguration extends RedisSentinelConfiguration {

    public void setSentinelsDesc(String desc) {
        A a = new Gson().fromJson(desc, A.class);
        super.setMaster(a.master);
        super.setSentinels(MyRedisClusterConfiguration.toRedisNodes(a.sentinels, 26379));
    }

    private static class A {
        private String master;
        private String[] sentinels;
    }

}
