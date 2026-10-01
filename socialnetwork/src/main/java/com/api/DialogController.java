package com.api;

import com.dto.SendDialogMessageRequest;
import com.service.DialogService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

@RestController
@RequiredArgsConstructor
@RequestMapping("/dialog")
public class DialogController {

    private final DialogService dialogService;

    @PostMapping("/{user_id}/send")
    public void send(@PathVariable String user_id,
                     @RequestBody SendDialogMessageRequest request,
                     Authentication authentication) throws InterruptedException {

        String senderId = authentication.getName();
        if (senderId == null) {
            throw new RuntimeException("Unauthorized");
        }

        doDelayOrThrowException();

        dialogService.sendMessage(senderId, user_id, request);
    }


    @GetMapping("/{user_id}/list")
    public List<DialogMessageResponse> list(@PathVariable String user_id,
                                            Authentication authentication) throws InterruptedException {
        String senderId = authentication.getName();
        if (senderId == null) {
            throw new RuntimeException("Unauthorized");
        }

        doDelayOrThrowException();

        return dialogService.listMessages(senderId, user_id);
    }

    /**
     * Специальные случайные события для работы с метриками.
     */
    private void doDelayOrThrowException() throws InterruptedException {
        int random1 = ThreadLocalRandom.current().nextInt(1, 11);

        if (random1 == 1) {
            long sleepMs = ThreadLocalRandom.current().nextLong(300, 2001);
            Thread.sleep(sleepMs);
        }

        int random2 = ThreadLocalRandom.current().nextInt(1, 6);

        if (random2 == 1) {
            throw new RuntimeException("Random test exception");
        }
    }

}
