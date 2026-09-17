package org.counter.controller;

import lombok.RequiredArgsConstructor;
import org.counter.repository.UserMessageCounter;
import org.counter.service.CounterService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/counters")
public class CounterController {

    private final CounterService counterService;

    /**
     * Счётчик непрочитанных сообщений пользователя.
     * Сначала Redis; при промахе — БД с последующим обновлением Redis.
     */
    @GetMapping("/{userId}")
    public ResponseEntity<UserMessageCounter> getUnreadCount(@PathVariable String userId) {
        return ResponseEntity.ok(counterService.getCounter(userId));
    }

}
