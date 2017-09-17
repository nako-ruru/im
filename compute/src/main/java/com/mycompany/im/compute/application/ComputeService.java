package com.mycompany.im.compute.application;

import com.mycompany.im.compute.domain.ComputeKernel;
import com.mycompany.im.compute.domain.FromConnectorMessage;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Collection;

/**
 * Created by Administrator on 2017/8/28.
 */
@Service
public class ComputeService {

    @Resource
    private ComputeKernel computeKernel;

    public void compute(Collection<FromConnectorMessage> messages) {
        computeKernel.compute(messages);
    }

}
