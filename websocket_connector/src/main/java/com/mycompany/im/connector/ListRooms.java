/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mycompany.im.connector;

import com.mycompany.im.work.framework.spring.SpringContext;
import javax.websocket.Session;

/**
 *
 * @author Administrator
 */
public class ListRooms {
    
    private final Session session;
    
    public ListRooms(Session session) {
        this.session = session;
    }
    
    public void invoke() {
        SpringContext.getBean(Game.class).rooms(session);
    }
    
}
