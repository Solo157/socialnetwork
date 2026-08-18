package com.api;

import com.dto.CreatePostRequest;
import com.dto.UpdatePostRequest;
import com.service.PostService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.*;

/**
 * Контроллер по постам пользователей.
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/post")
public class PostController {

    private final PostService postService;

    @PostMapping("/create")
    public Map<String, String> create(@RequestBody CreatePostRequest request, Authentication authentication) {
        String authorId = authentication.getName();
        if (authorId == null) {
            throw new RuntimeException("Unauthorized");
        }

        String postId = postService.create(authorId, request);
        return Map.of("postId", postId);
    }

    @PutMapping("/update")
    public void update(@RequestBody UpdatePostRequest request, Authentication authentication) {
        if (authentication.getName() == null) {
            throw new RuntimeException("Unauthorized");
        }

        postService.update(request);
    }

    @PutMapping("/delete/{id}")
    public void delete(@PathVariable String id, Authentication authentication) {
        if (authentication.getName() == null) {
            throw new RuntimeException("Unauthorized");
        }

        postService.delete(id);
    }

    @GetMapping("/get/{id}")
    public PostResponse get(@PathVariable String id) {
        return postService.get(id);
    }

    @GetMapping("/feed")
    public List<PostResponse> feed(
            Authentication authentication,
            @RequestParam(value = "offset", defaultValue = "0") int offset,
            @RequestParam(value = "limit", defaultValue = "10") int limit) {
        String userId = authentication.getName();
        if (userId == null) {
            throw new RuntimeException("Unauthorized");
        }

        return postService.getFeed(userId, offset, limit);
    }

}
