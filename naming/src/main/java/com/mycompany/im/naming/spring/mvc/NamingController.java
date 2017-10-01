/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mycompany.im.naming.spring.mvc;

import com.mycompany.im.naming.application.NamingQuery;
import com.mycompany.im.naming.application.NamingQueryResult;
import java.util.List;
import javax.annotation.Resource;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

/**
 *
 * @author Administrator
 */
@RestController
public class NamingController {
    
    @Resource
    private NamingQuery namingQuery;
    
    @RequestMapping(method = RequestMethod.GET)
    public List<NamingQueryResult> servers(String serverType) {
        return namingQuery.servers(serverType);
    }
}
