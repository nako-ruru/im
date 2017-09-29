package com.mycompany.im.naming.domain;

import java.util.List;

/**
 * Created by Administrator on 2017/9/3.
 */
public interface NamingRepository {
    List<String> servers(String serverType);
}
