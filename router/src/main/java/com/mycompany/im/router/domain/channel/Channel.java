package com.mycompany.im.router.domain.channel;

import com.mycompany.im.router.domain.Payload;

/**
 * Created by Administrator on 2017/9/2.
 */
public interface Channel {

    void send(Payload message);

}
