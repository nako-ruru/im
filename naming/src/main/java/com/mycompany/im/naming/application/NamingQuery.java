package com.mycompany.im.naming.application;

import com.mycompany.im.naming.domain.NamingRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by Administrator on 2017/8/28.
 */
@Service
public class NamingQuery {

    private final Logger logger = LoggerFactory.getLogger(getClass());
    
    @Resource
    private NamingRepository namingRepository;

    @Cacheable
    public List<NamingQueryResult> servers(String serverType) {
        return namingRepository.servers(serverType).stream()
                .map(info -> new NamingQueryResult(info.address, info.registerTime, info.loginUsers, info.connectedClients))
                .collect(Collectors.toCollection(LinkedList::new ));
    }

}
