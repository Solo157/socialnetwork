package org.dialog.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.dialog.repository.DialogMessageEntity;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.RedisScript;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@RequiredArgsConstructor
public class DialogCacheService {

    /**
     * Максимальное количество минут в редисе для хранения сообщений.
     */
    private static final long TTL_MINUTES = 30;

    private static final String MESSAGES_KEY_PREFIX = "dialog:messages:";
    private static final String DIALOG_PAIR_KEY_PREFIX = "dialog:pair:";

    private final RedisTemplate<String, String> stringRedisTemplate;
    private final RedisScript<String> getDialogIdScript;
    private final RedisScript<String> findOrCreateDialogScript;
    private final RedisScript<String> addMessageScript;
    private final RedisScript<List> getMessagesScript;

    private final ObjectMapper objectMapper;

    /**
     * Получить сообщения диалога.
     */
    public List<DialogMessageEntity> getMessages(String senderId, String receiverId) {
        String dialogId = getDialogId(senderId, receiverId);
        if (dialogId == null) {
            return List.of();
        }

        String key = MESSAGES_KEY_PREFIX + dialogId;

        List<String> result;
        try {
            result = (List<String>) stringRedisTemplate.execute(
                    getMessagesScript,
                    List.of(key)
            );
        } catch (Exception e) {
            throw new RuntimeException("Failed to get messages from Redis", e);
        }


        if (result.isEmpty()) {
            return Collections.emptyList();
        }

        List<DialogMessageEntity> entities = new ArrayList<>();
        for (String json : result) {
            try {
                entities.add(objectMapper.readValue(json, DialogMessageEntity.class));
            } catch (Exception e) {
                throw new RuntimeException("Failed to deserialize message", e);
            }
        }
        return entities;
    }

    /**
     * Добавить сообщение в диалог.
     */
    public void addMessage(String dialogId, DialogMessageEntity message) {
        String key = MESSAGES_KEY_PREFIX + dialogId;

        try {
            stringRedisTemplate.execute(
                    addMessageScript,
                    List.of(key),
                    objectMapper.writeValueAsString(message),
                    String.valueOf(
                            TTL_MINUTES * 60
                    )
            );
        } catch (Exception e) {
            throw new RuntimeException("Failed to add message to Redis", e);
        }
    }

    /**
     * Найти и получить идентификатор диалога по идентификаторам пользователей. Идентификатор диалога может храниться
     * под разными комбинациями связки идентификаторов пользователей.
     */
    private String getDialogId(String userId1, String userId2) {
        String key1 = DIALOG_PAIR_KEY_PREFIX + userId1 + ":" + userId2;
        String key2 = DIALOG_PAIR_KEY_PREFIX + userId2 + ":" + userId1;

        try {
            return stringRedisTemplate.execute(getDialogIdScript, List.of(key1, key2));
        } catch (Exception e) {
            throw new RuntimeException("Failed to get dialogId from Redis", e);
        }
    }

    public String findOrCreateDialogId(String userId1, String userId2) {
        String key1 = DIALOG_PAIR_KEY_PREFIX + userId1 + ":" + userId2;
        String key2 = DIALOG_PAIR_KEY_PREFIX + userId2 + ":" + userId1;
        String randomDialogId = UUID.randomUUID().toString();

        try {
            return stringRedisTemplate.execute(
                    findOrCreateDialogScript,
                    List.of(key1, key2),
                    randomDialogId,
                    String.valueOf(TTL_MINUTES * 60)
            );
        } catch (Exception e) {
            throw new RuntimeException("Failed to find or create dialog in Redis", e);
        }
    }

}
