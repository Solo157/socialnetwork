package com.service;

import com.database.PostEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class FeedNotificationService {

    private final SimpMessagingTemplate messagingTemplate;

    /**
     * Отправляет конкретному пользователю у которого есть веб-сокет сессия о том, что пост создан/обновлен.
     * Данный пост нужно отрпавить пользователю по WS.
     */
    public void notifyUser(String userId, PostEntity post) {
        messagingTemplate.convertAndSendToUser(
                userId,
                "/post/feed/posted",
                post
        );
    }

}
