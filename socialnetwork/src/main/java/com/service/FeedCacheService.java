package com.service;

import com.database.PostEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class FeedCacheService {

    private static final int MAX_FEED_SIZE = 1000;
    private static final long TTL_MINUTES = 3;

    private final RedisTemplate<String, Object> redisTemplate;
    private final String FEED_KEY = "feed:"; // такой будет ключ в редисе

    public void saveValue(String userId, List<PostEntity> value) {
        String feedKey = FEED_KEY + userId;
        List<PostEntity> trimmed = trimToLast(value, MAX_FEED_SIZE);
        redisTemplate.opsForValue().set(feedKey, trimmed, TTL_MINUTES, TimeUnit.MINUTES);
    }

    public List<PostEntity> getValue(String userId) {
        String feedKey = FEED_KEY + userId;
        Object obj = redisTemplate.opsForValue().get(feedKey);
        if (obj instanceof List) {
            return (List<PostEntity>) obj;
        }
        return null;
    }

    public void addValue(String userId, PostEntity value) {
        String feedKey = FEED_KEY + userId;
        List<PostEntity> posts = getValue(userId);
        if (posts == null) {
            posts = new ArrayList<>();
        }
        posts.add(value);
        sortAsc(posts);
        posts = trimToLast(posts, MAX_FEED_SIZE);
        redisTemplate.opsForValue().set(feedKey, posts, TTL_MINUTES, TimeUnit.MINUTES);
    }

    public void updateValue(String userId, PostEntity updated) {
        String feedKey = FEED_KEY + userId;
        List<PostEntity> posts = getValue(userId);
        if (posts == null) {
            return;
        }
        for (int i = 0; i < posts.size(); i++) {
            if (posts.get(i).getId().equals(updated.getId())) {
                posts.set(i, updated);
                break;
            }
        }
        sortAsc(posts);
        redisTemplate.opsForValue().set(feedKey, posts, TTL_MINUTES, TimeUnit.MINUTES);
    }

    public void removeValue(String userId, String postId) {
        String feedKey = FEED_KEY + userId;
        List<PostEntity> posts = getValue(userId);
        if (posts == null) {
            return;
        }
        posts.removeIf(p -> p.getId().equals(postId));
        redisTemplate.opsForValue().set(feedKey, posts, TTL_MINUTES, TimeUnit.MINUTES);
    }

    public void deleteValue(String userId) {
        String feedKey = FEED_KEY + userId;
        redisTemplate.delete(feedKey);
    }

    private List<PostEntity> trimToLast(List<PostEntity> list, int max) {
        if (list.size() <= max) {
            return list;
        }
        return new ArrayList<>(list.subList(list.size() - max, list.size()));
    }

    private void sortAsc(List<PostEntity> list) {
        list.sort(Comparator.comparing(PostEntity::getCreatedAt));
    }

}
