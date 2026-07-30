package com.api;

import com.dto.SendDialogMessageRequest;
import com.service.DialogService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/dialog")
public class DialogController {

    private final DialogService dialogService;

    @PostMapping("/{user_id}/send")
    public void send(@PathVariable String user_id,
                     @RequestBody SendDialogMessageRequest request,
                     Authentication authentication) {

        String senderId = authentication.getName();
        if (senderId == null) {
            throw new RuntimeException("Unauthorized");
        }

        dialogService.sendMessage(senderId, user_id, request);
    }

    @GetMapping("/{user_id}/list")
    public List<DialogMessageResponse> list(@PathVariable String user_id, Authentication authentication) {
        String senderId = authentication.getName();
        if (senderId == null) {
            throw new RuntimeException("Unauthorized");
        }

        return dialogService.listMessages(senderId, user_id);
    }

}
