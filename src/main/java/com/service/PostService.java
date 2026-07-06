package com.service;

import com.api.PostResponse;
import com.database.PostEntity;
import com.database.PostRepository;
import com.database.UserRepository;
import com.dto.CreatePostRequest;
import com.dto.UpdatePostRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PostService {

    private final PostRepository postRepository;
    private final UserRepository userRepository;
    private final RedisService redisService;

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

        addToFeedsOfUsersWhoFriend(authorId, post);

        return id;
    }

    @Transactional(readOnly = false)
    public void update(UpdatePostRequest request) {
        PostEntity updated = postRepository.findById(request.getId())
                .orElseThrow(() -> new RuntimeException("Post not found"));

        updated.setText(request.getText());

        postRepository.update(request.getId(), request.getText());

        updateInFeedsOfUsersWhoFriend(updated.getAuthorId(), updated);
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
        List<PostEntity> cached = redisService.getValue(userId);
        if (cached == null || cached.isEmpty()) {
            System.out.println("Кэш пустой, первый запрос");
            cached = getFeedFromDB(userId);
            if (!cached.isEmpty()) {
                redisService.saveValue(userId, cached);
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

    @Transactional(readOnly = false)
    public List<PostEntity> getFeedFromDB(String userId) {
        List<String> friendIds = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"))
                .getFriends();
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

    private List<String> getUsersWhoFriend(String authorId) {
        return userRepository.findUsersWithFriend(authorId);
    }

    /**
     * Добавляем для всех друзей данный пост.
     */
    private void addToFeedsOfUsersWhoFriend(String authorId, PostEntity post) {
        for (String uid : getUsersWhoFriend(authorId)) {
            redisService.addValue(uid, post);
        }
    }

    /**
     * Обновляем для всех друзей пост.
     */
    private void updateInFeedsOfUsersWhoFriend(String authorId, PostEntity post) {
        for (String uid : getUsersWhoFriend(authorId)) {
            redisService.updateValue(uid, post);
        }
    }

    /**
     * Удаляем у всех друзей пользователя данный пост.
     */
    private void removeFromFeedsOfUsersWhoFriend(String authorId, String postId) {
        for (String uid : getUsersWhoFriend(authorId)) {
            redisService.removeValue(uid, postId);
        }
    }

}
