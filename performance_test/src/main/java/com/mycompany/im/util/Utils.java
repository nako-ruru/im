/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mycompany.im.util;

import java.util.function.Function;

/**
 *
 * @author Administrator
 */
class Utils {

    public static <T> T getOrDefault(String[] args, int i, Function<String, T> func, T defaultValue) {
        return i < args.length ? func.apply(args[i]) : defaultValue;
    }
    
}
