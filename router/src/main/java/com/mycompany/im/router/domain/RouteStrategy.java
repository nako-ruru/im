package com.mycompany.im.router.domain;

import com.mycompany.im.router.domain.channel.Channel;

/**
 * Created by Administrator on 2017/9/2.
 */
public interface RouteStrategy {

    Channel route(RouteObject message) ;

}
