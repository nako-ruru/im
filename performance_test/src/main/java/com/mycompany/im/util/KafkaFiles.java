/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mycompany.im.util;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;

/**
 *
 * @author Administrator
 */
public class KafkaFiles {
    
    public static final String CONTENT = "{\"type\":3,\"data\":{\"gift\":\"{\\\"id\\\":10203,\\\"iconUrl\\\":\\\"http://oxm0z4x1c.bkt.clouddn.com/icon_10203.png\\\",\\\"previewUrl\\\":\\\"http://oxm0z4x1c.bkt.clouddn.com/preview_10203.zip\\\",\\\"name\\\":\\\"俄罗斯美男\\\",\\\"levelRequire\\\":0,\\\"priceType\\\":0,\\\"price\\\":1,\\\"playType\\\":1,\\\"state\\\":0,\\\"world\\\":false,\\\"guard\\\":false,\\\"expensive\\\":false,\\\"batch\\\":true,\\\"game\\\":false}\",\"simpleroom\":\"{\\\"uid\\\":113,\\\"medium\\\":143113,\\\"audienceCount\\\":6}\",\"roommember\":\"{\\\"id\\\":\\\"2731df23fafd44c8a870331a4f77c3ea\\\",\\\"uid\\\":29,\\\"actorId\\\":113,\\\"state\\\":0,\\\"roomRole\\\":4,\\\"medium\\\":160,\\\"enterTime\\\":1508929864573,\\\"simpleUser\\\":{\\\"id\\\":29,\\\"nickname\\\":\\\"JH HG GGF\\\",\\\"level\\\":2}}\"}}";
    
    public static void main(String... args) throws IOException {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter("kafka.txt"))) {
            for(int i = 0; i < 2_000_0000; i++) {
                writer.write(CONTENT);
                writer.newLine();
                if(i % 100 == 0) {
                    System.out.println(i);
                }
            }
        }
    }
}
