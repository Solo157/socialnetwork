package com.service;

import com.api.DialogMessageResponse;
import com.database.DialogMessageEntity;
import com.database.UserRepository;
import com.dto.SendDialogMessageRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class DialogService {

    private final DialogCacheService dialogCacheService;
    private final UserRepository userRepository;

    public void sendMessage(String senderId, String receiverId, SendDialogMessageRequest request) {
        userRepository.findById(receiverId)
                .orElseThrow(() -> new RuntimeException("Receiver not found"));

        String dialogId = findOrCreateDialogId(senderId, receiverId);

        DialogMessageEntity message = DialogMessageEntity.builder()
                .id(UUID.randomUUID().toString())
                .dialogId(dialogId)
                .senderId(senderId)
                .receiverId(receiverId)
                .text(request.getText())
                .createdAt(LocalDateTime.now())
                .build();

        System.out.println("sendMessage " + message.getDialogId() + " " + message.getText());

        dialogCacheService.addMessage(dialogId, message);
    }

    public List<DialogMessageResponse> listMessages(String senderId, String receiverId) {
        userRepository.findById(receiverId)
                .orElseThrow(() -> new RuntimeException("Receiver not found"));

        String dialogId = dialogCacheService.getDialogId(senderId, receiverId);
        if (dialogId == null) {
            return List.of();
        }

        return dialogCacheService.getMessages(dialogId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    private DialogMessageResponse toResponse(DialogMessageEntity message) {
        return DialogMessageResponse.builder()
                .id(message.getId())
                .dialogId(message.getDialogId())
                .senderId(message.getSenderId())
                .receiverId(message.getReceiverId())
                .text(message.getText())
                .createdAt(message.getCreatedAt())
                .build();
    }

    private String findOrCreateDialogId(String userId1, String userId2) {
        String existing = dialogCacheService.getDialogId(userId1, userId2);
        if (existing != null) {
            return existing;
        }

        String id = UUID.randomUUID().toString();
        dialogCacheService.saveDialogId(userId1, userId2, id);
        return id;
    }

}
