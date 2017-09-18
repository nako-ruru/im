package com.mycompany.im.router.domain;

import com.google.gson.Gson;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by Administrator on 2017/9/2.
 */
@Component
public class Router {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    @Resource
    private RouteStrategy routeStrategy;

    public void route(RouteObject routeObject) {
        logger.info("route: " + new Gson().toJson(routeObject));
        routeStrategy.route(routeObject).send(routeObject.getPayload());
    }

}
