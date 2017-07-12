package com.mycompany.im.connector;

import org.junit.Test;

/**
 * Created by Administrator on 2017/6/17.
 */
public class AsciiTest {

    @Test
    public void a() {
        byte[] bytes = {
                123,
                34,
                73,
                68,
                34,
                58,
                34,
                34,
                44,
                34,
                80,
                97,
                115,
                115,
                34,
                58,
                34,
                34,
                125,
        };
        System.out.println(new String(bytes));
    }

}
