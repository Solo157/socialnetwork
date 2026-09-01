package com.service;

import com.database.PostEntity;
import com.rabbit.PostCreatedEvent;
import com.rabbit.PostUpdatedEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * Сервис материализации. Обновляет посты пользователей и уведомляет пользователей.
 */
@Service
@RequiredArgsConstructor
public class FeedMaterializationService {

    private final FeedNotificationService feedNotificationService;
    private final UserService userService;
    private final PostService postService;

    /**
     * Материализация, что пост создан. Добавляет пост к друзьям пользователя.
     * Уведомляет друзей о новом посте.
     */
    public void materializePostCreatedEvent(PostCreatedEvent event) {
        System.out.println("Received post created event: " + event);

        List<String> friends = userService.getUsersWhoFriend(event.getAuthorId());
        postService.addToFeedsOfUsersWhoFriend(event.getPost(), friends);
        notifyFriends(friends, event.getPost());
    }

    /**
     * Материализация, что пост обновлен. Обновляет пост у друзей пользователя.
     * Уведомляет друзей об обновленном посте.
     */
    public void materializePostUpdatedEvent(PostUpdatedEvent event) {
        System.out.println("Received post updated event: " + event);

        List<String> friends = userService.getUsersWhoFriend(event.getAuthorId());
        postService.updateInFeedsOfUsersWhoFriend(event.getPost(), friends);
        notifyFriends(friends, event.getPost());
    }

    private void notifyFriends(List<String> friends, PostEntity event) {
        for (String friendId : friends) {
            feedNotificationService.notifyUser(friendId, event);
        }
    }

}
