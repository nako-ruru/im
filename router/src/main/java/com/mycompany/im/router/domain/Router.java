package com.mycompany.im.router.domain;

import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * Created by Administrator on 2017/9/2.
 */
@Component
public class Router {

    @Resource
    private RouteStrategy routeStrategy;

    public void route(RouteObject routeObject) {
        routeStrategy.route(routeObject).send(routeObject.getPayload());
    }

}
