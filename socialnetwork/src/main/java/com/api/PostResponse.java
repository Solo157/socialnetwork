package com.api;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * DTO для возврата поста.
 */
@Data
@Builder
public class PostResponse {

    private String id;
    private String text;
    private String authorId;
    private LocalDateTime createdAt;

}
