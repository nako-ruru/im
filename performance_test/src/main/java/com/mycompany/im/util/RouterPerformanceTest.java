/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mycompany.im.util;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

/**
 *
 * @author Administrator
 */
public class RouterPerformanceTest {
    
    public static void main(String... args) {
        String routerHost = Utils.getOrDefault(args, 0, Function.identity(), "47.92.49.101:8080/router");
        String roomId = Utils.getOrDefault(args, 0, Function.identity(), "17b08c84-8e10-44d1-b93a-15128f182aa0");
        long intervalInMilli = Utils.getOrDefault(args, 1, Long::parseLong, 10L);
        
        ScheduledExecutorService scheduledExecutorService = Executors.newScheduledThreadPool(30);
        scheduledExecutorService.scheduleAtFixedRate(() -> {
            HttpURLConnection httpCon = null;
            try {
                final String u = String.format("http://%s/send?toRoomId=%s&importance=0&content=%s",
                        routerHost, 
                        URLEncoder.encode(roomId, "UTF-8"),
                        URLEncoder.encode(KafkaFiles.CONTENT, "UTF-8")
                );
                httpCon = (HttpURLConnection) new URL(u).openConnection();
                httpCon.setDoOutput(true);
                httpCon.setRequestMethod("POST");
                System.out.println(httpCon.getResponseCode());
                System.out.println(httpCon.getResponseMessage());
            } catch(IOException e) {
                e.printStackTrace();
            } finally {
                if(httpCon != null) {
                    httpCon.disconnect();
                }
            }
        }, intervalInMilli, intervalInMilli, TimeUnit.MILLISECONDS);
    }
    
}
