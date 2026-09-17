package org.counter.repository;

import lombok.*;

import java.time.LocalDateTime;

/**
 * Счётчик непрочитанных сообщений пользователя.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserMessageCounter {

    private String userId;
    private long unreadCount;
    private LocalDateTime lastMessageAt;

}
