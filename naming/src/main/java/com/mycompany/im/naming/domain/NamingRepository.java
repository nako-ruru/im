package com.mycompany.im.naming.domain;

import java.util.List;

/**
 * Created by Administrator on 2017/9/3.
 */
public interface NamingRepository {
    
    List<NamingInfo> servers(String serverType);
    
    class NamingInfo {
        public String address;
        public long registerTime;
        public int loginUsers;
        public int connectedClients;
    }
}
