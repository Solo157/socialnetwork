package com.api;

import com.dto.CreatePostRequest;
import com.dto.UpdatePostRequest;
import com.service.PostService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
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
    public Map<String, String> create(@RequestBody CreatePostRequest request, HttpServletRequest httpRequest) {
        String authorId = (String) httpRequest.getAttribute("user_id");
        if (authorId == null) {
            throw new RuntimeException("Unauthorized");
        }

        String postId = postService.create(authorId, request);
        return Map.of("postId", postId);
    }

    @PutMapping("/update")
    public void update(@RequestBody UpdatePostRequest request, HttpServletRequest httpRequest) {
        if (httpRequest.getAttribute("user_id") == null) {
            throw new RuntimeException("Unauthorized");
        }

        postService.update(request);
    }

    @PutMapping("/delete/{id}")
    public void delete(@PathVariable String id, HttpServletRequest httpRequest) {
        if (httpRequest.getAttribute("user_id") == null) {
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
            HttpServletRequest httpRequest,
            @RequestParam(value = "offset", defaultValue = "0") int offset,
            @RequestParam(value = "limit", defaultValue = "10") int limit) {
        String userId = (String) httpRequest.getAttribute("user_id");
        if (userId == null) {
            throw new RuntimeException("Unauthorized");
        }

        return postService.getFeed(userId, offset, limit);
    }

}
