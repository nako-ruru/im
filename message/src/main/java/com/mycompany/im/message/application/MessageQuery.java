package com.mycompany.im.message.application;

import com.google.common.collect.ImmutableMap;
import com.google.common.util.concurrent.Striped;
import com.mycompany.im.message.domain.Message;
import com.mycompany.im.message.domain.MessageRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.stream.Collectors;

/**
 * Created by Administrator on 2017/7/31.
 */
@Service
public class MessageQuery {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    @Resource
    private MessageRepository messageRepository;

    private final Map<String, CacheObject<List<Message>>> cache = new ConcurrentHashMap<>();
    private final Striped<ReadWriteLock> striped = Striped.lazyWeakReadWriteLock(128);

    public List<MessageResult> findByRoomIdAndFromGreaterThan(MessageParameter parameter) {
        if(parameter.getFrom() == 0) {
            return newEmptyMessages(parameter.getRoomId());
        }

        ReadWriteLock lock = striped.get(parameter.getRoomId());

        CacheObject<List<Message>> cacheObject;
        try {
            if(lock.readLock().tryLock(1000L, TimeUnit.MILLISECONDS)) {
                try {
                    cacheObject = cache.get(parameter.getRoomId());
                } finally {
                    lock.readLock().unlock();
                }
            } else {
                return newEmptyMessages(parameter.getRoomId());
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            logger.error("", e);
            return newEmptyMessages(parameter.getRoomId());
        }

        if(cacheObject == null || cacheObject.isExpired()) {
            lock.writeLock().lock();
            try {
                cacheObject = cache.get(parameter.getRoomId());
                if(cacheObject == null || cacheObject.isExpired()) {
                    List<Message> messages = messageRepository.findByRoomIdAndFromGreaterThan(parameter.getRoomId(), Math.min(parameter.getFrom(), 1));
                    cache.put(parameter.getRoomId(), cacheObject = new CacheObject<>(messages));
                }
            } finally {
                lock.writeLock().unlock();
            }
        }

        List<Message> messages = cacheObject.o;
        if(messages.isEmpty()) {
            return newEmptyMessages(parameter.getRoomId());
        }
        return messages.stream()
                .filter(m -> m.getTime() >= parameter.getFrom())
                .map(MessageQuery::convert)
                .collect(Collectors.toCollection(LinkedList::new));
    }

    @PostConstruct
    private void postConstruct() {
        new Thread(() -> {
            do {
                try {
                    purge();
                } catch (Exception e) {
                    logger.error("", e);
                }
                try {
                    TimeUnit.MINUTES.sleep(10);
                } catch (InterruptedException e) {
                    logger.error("", e);
                }
            } while (true);
        }, "message-cache-purge").start();
    }

    private void purge() {
        for (Map.Entry<String, CacheObject<List<Message>>> entry : cache.entrySet()) {
            ReadWriteLock lock = striped.get(entry.getKey());
            lock.writeLock().lock();
            try {
                if(entry.getValue().isExpired()) {
                    cache.remove(entry.getKey(), entry.getValue());
                }
            } finally {
                lock.writeLock().unlock();
            }
        }
    }

    private static MessageResult convert(Message message) {
        return new MessageResult(
                message.getMessageId(), 
                message.getToRoomId(),
                message.getFromUserId(),
                message.getType(),
                message.getParams(),
                message.getFromNickname(),
                message.getFromLevel(),
                message.getTime()
        );
    }

    private static List<MessageResult> newEmptyMessages(String roomId) {
        MessageResult result = new MessageResult(
                "",
                roomId,
                "",
                10001,
                ImmutableMap.of(),
                "",
                0,
                System.currentTimeMillis()
        );
        return Arrays.asList(result);
    }

    private static class CacheObject<V> {
        private long cacheTime = System.currentTimeMillis();
        private final long expiryTime = 500L;
        private final V o;
        public CacheObject(V o) {
            this.o = o;
        }
        public boolean isExpired() {
            return (System.currentTimeMillis() - cacheTime) > expiryTime;
        }
    }

}
