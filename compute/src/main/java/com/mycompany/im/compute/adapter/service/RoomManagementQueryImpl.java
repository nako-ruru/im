package com.mycompany.im.compute.adapter.service;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.mycompany.im.compute.domain.RoomManagementQuery;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

/**
 * Created by Administrator on 2017/8/20.
 */
@Service
public class RoomManagementQueryImpl implements RoomManagementQuery {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    @Override
    public RoomManagementInfo query() {
        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(1000);
        requestFactory.setReadTimeout(1000);
        RestTemplate restTemplate = new RestTemplate(requestFactory);

        Multimap<String, String> kickList = HashMultimap.create();
        Multimap<String, String> silenceList = HashMultimap.create();

        try {
            Entity[] entities = restTemplate.getForObject("http://121.42.181.209:1119/member/list/bound", Entity[].class);

            for(Entity entity : entities) {
                if(entity.state == 2) {
                    kickList.put(entity.actorId, entity.uid);
                }
                else if(entity.state == 3) {
                    silenceList.put(entity.actorId, entity.uid);
                }
                else {
                    logger.error("unknown state: " + entity.state);
                }
            }

        } catch (ResourceAccessException e) {
            logger.error("", e);
        }
        return new RoomManagementInfo(kickList, silenceList);
    }

    public static class Entity {

        private String uid, actorId;
        int state;

        public String getUid() {
            return uid;
        }

        public void setUid(String uid) {
            this.uid = uid;
        }

        public String getActorId() {
            return actorId;
        }

        public void setActorId(String actorId) {
            this.actorId = actorId;
        }

        public int getState() {
            return state;
        }

        public void setState(int state) {
            this.state = state;
        }

    }

}
