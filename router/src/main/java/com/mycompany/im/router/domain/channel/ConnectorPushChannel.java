package com.mycompany.im.router.domain.channel;

import com.mycompany.im.router.domain.Payload;
import javax.annotation.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Created by Administrator on 2017/9/2.
 */
@Component
public class ConnectorPushChannel implements Channel {

    private final Logger logger = LoggerFactory.getLogger(getClass());
    
    @Resource
    private Push push;
    
    @Override
    public void send(Payload message) {
        push.send(message);
    }
    
}
