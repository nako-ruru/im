package com.mycompany.im.naming.application;

import com.mycompany.im.naming.domain.NamingRepository;
import java.util.List;
import javax.annotation.Resource;
import org.springframework.stereotype.Service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by Administrator on 2017/8/28.
 */
@Service
public class NamingQuery {

    private final Logger logger = LoggerFactory.getLogger(getClass());
    
    @Resource
    private NamingRepository namingRepository;

    public List<String> servers(String serverType) {
        return namingRepository.servers(serverType);
    }

}
