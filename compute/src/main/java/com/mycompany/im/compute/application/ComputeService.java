package com.mycompany.im.compute.application;

import com.jsoniter.output.JsonStream;
import com.jsoniter.spi.Config;
import com.mycompany.im.compute.domain.ComputeKernel;
import com.mycompany.im.compute.domain.FromConnectorMessage;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Collection;
import java.util.LinkedList;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by Administrator on 2017/8/28.
 */
@Service
public class ComputeService {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    @Resource
    private ComputeKernel computeKernel;

    public void compute(Collection<FromConnectorMessage> messages) {
        if(!messages.isEmpty()) {
            Collection<FromConnectorMessage> limit = messages.stream()
                    .limit(10)
                    .collect(Collectors.toCollection(LinkedList::new));
            logger.info(" [x] Received '" + JsonStream.serialize(new Config.Builder().escapeUnicode(false).build(), limit)+ "'...");
            computeKernel.compute(messages);
        }
    }

}
