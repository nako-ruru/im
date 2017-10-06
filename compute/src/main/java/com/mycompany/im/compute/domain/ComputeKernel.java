package com.mycompany.im.compute.domain;

import com.google.common.collect.Multimap;
import com.google.common.collect.Multimaps;
import com.mycompany.im.compute.adapter.persistence.redis.RedisMessageRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.Objects;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.stream.Collectors;

/**
 * 该类订阅从connector发来的某玩家消息，并判断该玩家是否被禁言或踢掉，如果没有则存储到redis里
 */
@Component
public class ComputeKernel {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private final Multimap<String, String> silencedList = newConcurrentHashMultimap();
    private final Multimap<String, String> kickedList = newConcurrentHashMultimap();

    private final BlockingQueue<ToPollingMessage> queue = new LinkedBlockingQueue<>();

    @Resource
    private KeywordHandler keywordHandler;
    @Resource
    private RoomManagementQuery roomManagementQuery;
    @Resource
    private RedisMessageRepository messageRepository;

    public void start() {
        RoomManagementQuery.RoomManagementInfo roomManagementInfo = roomManagementQuery.query();
        silencedList.putAll(roomManagementInfo.getSilenceList());
        kickedList.putAll(roomManagementInfo.getKickList());

        new Thread(() -> {
            int maxPollingSize = 10_0000;
            do {
                try {
                    ToPollingMessage first = queue.take();
                    Collection<ToPollingMessage> messages = new LinkedList();
                    messages.add(first);
                    queue.drainTo(messages, maxPollingSize - 1);

                    logger.info(String.format("poll size: %s; queue size: %s", messages.size(), queue.size()));

                    messageRepository.save(messages);
                } catch (InterruptedException e) {
                    logger.error("", e);
                } catch (Exception e) {
                    logger.error("", e);
                }
            } while(true);
        }, "redis-save").start();
    }

    public void compute(Collection<FromConnectorMessage> message) {
        handleIncomingMessage(message, keywordHandler);
    }
    
    public void addKick(String roomId, String userId) {
        kickedList.put(roomId, userId);
    }
    
    public void removeKick(String roomId, String userId) {
        kickedList.remove(roomId, userId);
    }
    
    public void addSilence(String roomId, String userId) {
        silencedList.put(roomId, userId);
    }
    
    public void removeSilence(String roomId, String userId) {
        silencedList.remove(roomId, userId);
    }
    
    public void clearSilence(String roomId) {
        silencedList.removeAll(roomId);
    }

    private void handleIncomingMessage(Collection<FromConnectorMessage> messages, KeywordHandler keywordHandler) {
        Collection<ToPollingMessage> toPollingMessages = messages.stream()
                .filter(msg -> !silencedList.containsEntry(msg.roomId, msg.userId) && !kickedList.containsEntry(msg.roomId, msg.userId))
                .map(msg -> {
                    String oldContent = msg.params.get("content");
                    String newContent = keywordHandler.handle(oldContent);
                    if(!Objects.equals(newContent, oldContent)) {
                        msg.params.put("content", newContent);
                    }
                    return msg;
                })
                .map(msg -> new ToPollingMessage(msg.messageId, msg.roomId, msg.userId, msg.nickname, msg.level, msg.type, msg.params))
                .collect(Collectors.toCollection(LinkedList::new));

        queue.addAll(toPollingMessages);
    }
    
    private static <K, V> Multimap<K, V> newConcurrentHashMultimap() {
        return Multimaps.newMultimap(new ConcurrentHashMap<>(), () -> Collections.newSetFromMap(new ConcurrentHashMap<>()));
    }
    
}
