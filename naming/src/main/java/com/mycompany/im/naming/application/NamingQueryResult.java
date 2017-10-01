/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mycompany.im.naming.application;

/**
 *
 * @author Administrator
 */
public class NamingQueryResult {
    
    private final String address;
    private final long registerTime;
    private final int loginUsers;
    private final int connectedClients;

    public NamingQueryResult(String address, long registerTime, int loginUsers, int connectedClients) {
        this.address = address;
        this.registerTime = registerTime;
        this.loginUsers = loginUsers;
        this.connectedClients = connectedClients;
    }

    public String getAddress() {
        return address;
    }

    public long getRegisterTime() {
        return registerTime;
    }

    public int getLoginUsers() {
        return loginUsers;
    }

    public int getConnectedClients() {
        return connectedClients;
    }
    
}
