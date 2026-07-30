package com.service;

import com.adapter.PostEventSender;
import com.api.PostResponse;
import com.database.PostEntity;
import com.database.PostRepository;
import com.dto.CreatePostRequest;
import com.dto.UpdatePostRequest;
import com.rabbit.PostCreatedEvent;
import com.rabbit.PostUpdatedEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
public class PostService {

    private final PostRepository postRepository;
    private final UserService userService;
    private final FeedCacheService feedCacheService;
    private final PostEventSender postEventSender;

    @Transactional(readOnly = false)
    public String create(String authorId, CreatePostRequest request) {
        String id = UUID.randomUUID().toString();

        PostEntity post = PostEntity.builder()
                .id(id)
                .text(request.getText())
                .authorId(authorId)
                .createdAt(LocalDateTime.now())
                .build();

        postRepository.save(post);

        PostCreatedEvent event = new PostCreatedEvent(authorId, post);
        postEventSender.sendPostCreatedEvent(event);

        return id;
    }

    @Transactional(readOnly = false)
    public void update(UpdatePostRequest request) {
        PostEntity updatedPost = postRepository.findById(request.getId())
                .orElseThrow(() -> new RuntimeException("Post not found"));

        updatedPost.setText(request.getText());
        postRepository.update(updatedPost);

        PostUpdatedEvent postUpdatedEvent = new PostUpdatedEvent(updatedPost.getAuthorId(), updatedPost);
        postEventSender.sendPostUpdatedEvent(postUpdatedEvent);
    }

    @Transactional(readOnly = false)
    public void delete(String id) {
        PostEntity post = postRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Post not found"));

        postRepository.delete(id);

        removeFromFeedsOfUsersWhoFriend(post.getAuthorId(), id);
    }

    @Transactional(readOnly = false)
    public PostResponse get(String id) {
        PostEntity post = postRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Post not found"));

        return toResponse(post);
    }

    @Transactional(readOnly = false)
    public List<PostResponse> getFeed(String userId, int offset, int limit) {
        List<PostEntity> cached = feedCacheService.getValue(userId);
        if (cached == null || cached.isEmpty()) {
            System.out.println("Кэш пустой, первый запрос");
            cached = getFeedFromDB(userId);
            if (!cached.isEmpty()) {
                feedCacheService.saveValue(userId, cached);
            }
            System.out.println("Кэш обновлен из БД, количество записей: " + cached.size());
        } else {
            System.out.println("Кэш взят из Redis, количество записей: " + cached.size());
        }

        int from = Math.min(offset, cached.size());
        int to = Math.min(from + limit, cached.size());
        List<PostEntity> page = cached.subList(from, to);
        return page.stream()
                .map(this::toResponse)
                .toList();
    }

    private List<PostEntity> getFeedFromDB(String userId) {
//        List<String> friendIds = userRepository.findById(userId)
//                .orElseThrow(() -> new RuntimeException("User not found"))
//                .getFriends();
        List<String> friendIds = userService.getUsersWhoFriend(userId);

        if (friendIds == null || friendIds.isEmpty()) {
            return List.of();
        }

        return postRepository.findPostsByAuthorIds(friendIds, 0, 1000);
    }

    private PostResponse toResponse(PostEntity p) {
        return PostResponse.builder()
                .id(p.getId())
                .text(p.getText())
                .authorId(p.getAuthorId())
                .createdAt(p.getCreatedAt())
                .build();
    }

    /**
     * Добавляем для всех друзей данный пост.
     */
    public void addToFeedsOfUsersWhoFriend(PostEntity post, List<String> friends) {
        for (String uid : friends) {
            feedCacheService.addValue(uid, post);
        }
    }

    /**
     * Обновляем для всех друзей пост.
     */
    public void updateInFeedsOfUsersWhoFriend(PostEntity post, List<String> friends) {
        for (String uid : friends) {
            feedCacheService.updateValue(uid, post);
        }
    }

    /**
     * Удаляем у всех друзей пользователя данный пост.
     */
    private void removeFromFeedsOfUsersWhoFriend(String authorId, String postId) {
        for (String uid : userService.getUsersWhoFriend(authorId)) {
            feedCacheService.removeValue(uid, postId);
        }
    }

}
