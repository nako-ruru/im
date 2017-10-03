/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mycompany.im.router.domain.channel;

import com.mycompany.im.router.domain.Payload;

/**
 *
 * @author Administrator
 */
public interface Push {
    void send(Payload message);
}
