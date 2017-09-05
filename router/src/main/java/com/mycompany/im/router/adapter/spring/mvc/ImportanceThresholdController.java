package com.mycompany.im.router.adapter.spring.mvc;

import com.mycompany.im.router.application.ImportanceThresholdCommand;
import com.mycompany.im.router.application.ImportanceThresholdService;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

/**
 * Created by Administrator on 2017/9/5.
 */
@RestController
public class ImportanceThresholdController {

    @Resource
    private ImportanceThresholdService importanceThresholdService;

    @RequestMapping(value = "/importance", method = RequestMethod.POST)
    public void send(ImportanceThresholdCommand command) {
        importanceThresholdService.setThreshold(command);
    }

}
