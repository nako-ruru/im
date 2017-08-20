package com.mycompany.im.compute.domain;

import com.google.common.collect.Multimap;
import com.google.gson.Gson;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Administrator on 2017/8/20.
 */
public interface RoomManagementQuery {

    RoomManagementInfo query();

    class RoomManagementInfo {

        private Multimap<String, String> kickList, silenceList;

        public RoomManagementInfo(Multimap<String, String> kickList, Multimap<String, String> silenceList) {
            this.kickList = kickList;
            this.silenceList = silenceList;
        }

        public Multimap<String, String> getKickList() {
            return kickList;
        }

        public Multimap<String, String> getSilenceList() {
            return silenceList;
        }

        @Override
        public String toString() {
            Map<String, Object> map = new HashMap<>();
            map.put("kickList", kickList.asMap());
            map.put("silenceList", silenceList.asMap());
            return new Gson().toJson(map);
        }

    }

}
