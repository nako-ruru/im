package com.mycompany.im.compute.application;

import com.mycompany.im.compute.domain.ComputeKernel;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 * Created by Administrator on 2017/8/28.
 */
@Service
public class ComputeService {

    @Resource
    private ComputeKernel computeKernel;

    public void compute(String message) {
        computeKernel.compute(message);
    }

}