package com.service;

import com.database.DialogMessageEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class DialogCacheService {

    /**
     * Максимальное количество минут в редисе для хранения сообщений.
     */
    private static final long TTL_MINUTES = 30;

    private static final String MESSAGES_KEY_PREFIX = "dialog:messages:";
    private static final String DIALOG_PAIR_KEY_PREFIX = "dialog:pair:";

    private final RedisTemplate<String, Object> redisTemplate;

    /**
     * Получить сообщения диалога.
     */
    public List<DialogMessageEntity> getMessages(String dialogId) {
        String key = MESSAGES_KEY_PREFIX + dialogId;

        Object obj = redisTemplate.opsForValue().get(key);
        if (obj instanceof List) {
            return (List<DialogMessageEntity>) obj;
        }

        return new ArrayList<>();
    }

    /**
     * Добавить сообщение в диалог.
     */
    public void addMessage(String dialogId, DialogMessageEntity message) {
        String key = MESSAGES_KEY_PREFIX + dialogId;

        List<DialogMessageEntity> messages = getMessagesFromRedis(dialogId);
        messages.add(message);
        messages.sort(Comparator.comparing(DialogMessageEntity::getCreatedAt));

        redisTemplate.opsForValue().set(key, messages, TTL_MINUTES, TimeUnit.MINUTES);
    }

    /**
     * Найти и получить идентификатор диалога по идентификаторам пользователей. Идентификатор диалога может храниться
     * под разными комбинациями связки идентификаторов пользователей.
     */
    public String getDialogId(String userId1, String userId2) {
        String key1 = DIALOG_PAIR_KEY_PREFIX + userId1 + ":" + userId2;
        String key2 = DIALOG_PAIR_KEY_PREFIX + userId2 + ":" + userId1;

        Object obj1 = redisTemplate.opsForValue().get(key1);
        if (obj1 instanceof String) {
            return (String) obj1;
        }

        Object obj2 = redisTemplate.opsForValue().get(key2);
        if (obj2 instanceof String) {
            return (String) obj2;
        }

        return null;
    }

    /**
     * Сохранить идентификатор диалога. Используется разные связки идентификаторов пользователей для хранения.
     */
    public void saveDialogId(String userId1, String userId2, String dialogId) {
        String key1 = DIALOG_PAIR_KEY_PREFIX + userId1 + ":" + userId2;
        String key2 = DIALOG_PAIR_KEY_PREFIX + userId2 + ":" + userId1;

        redisTemplate.opsForValue().set(key1, dialogId, TTL_MINUTES, TimeUnit.MINUTES);
        redisTemplate.opsForValue().set(key2, dialogId, TTL_MINUTES, TimeUnit.MINUTES);
    }

    /**
     * Получить все сообщения по диалогу.
     */
    private List<DialogMessageEntity> getMessagesFromRedis(String dialogId) {
        String key = MESSAGES_KEY_PREFIX + dialogId;

        Object obj = redisTemplate.opsForValue().get(key);
        if (obj instanceof List) {
            return (List<DialogMessageEntity>) obj;
        }

        return new ArrayList<>();
    }

}
