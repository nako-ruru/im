package com.mycompany.im.router.domain.strategy.threshold;

import com.google.common.base.Strings;
import com.mycompany.im.framework.spring.SpringContext;
import com.mycompany.im.router.domain.RouteObject;
import com.mycompany.im.router.domain.RouteStrategy;
import com.mycompany.im.router.domain.channel.Channel;
import com.mycompany.im.router.domain.channel.ConnectorPushChannel;
import com.mycompany.im.router.domain.channel.HttpPollingChannel;
import org.springframework.stereotype.Component;

/**
 * Created by Administrator on 2017/9/2.
 */
@Component
public class ThresholdRouteStrategy implements RouteStrategy {

    private int threshold = 0;

    @Override
    public Channel route(RouteObject message) {
        if(!Strings.isNullOrEmpty(message.getPayload().toUserId)) {
            return SpringContext.getBean(ConnectorPushChannel.class);
        }
        return message.getRank() <= threshold ?
                SpringContext.getBean(ConnectorPushChannel.class) :
                SpringContext.getBean(HttpPollingChannel.class);
    }

    public void setThreshold(int threshold) {
        this.threshold = threshold;
    }
    
}
