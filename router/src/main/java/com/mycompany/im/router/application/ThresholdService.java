package com.mycompany.im.router.application;

import com.mycompany.im.router.domain.strategy.threshold.ThresholdRouteStrategy;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * Created by Administrator on 2017/9/2.
 */
@Component
public class ThresholdService {

    @Resource
    private ThresholdRouteStrategy thresholdRouteStrategy;

    public void setThreshold(ThresholdCommand command) {
        thresholdRouteStrategy.setThreshold(command.getThreshold());
    }

}
