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

        String dialogId = dialogCacheService.findOrCreateDialogId(senderId, receiverId);

        DialogMessageEntity message = DialogMessageEntity.builder()
                .id(UUID.randomUUID().toString())
                .dialogId(dialogId)
                .senderId(senderId)
                .receiverId(receiverId)
                .text(request.getText())
                .createdAt(LocalDateTime.now())
                .build();

        dialogCacheService.addMessage(dialogId, message);
    }

    public List<DialogMessageResponse> listMessages(String senderId, String receiverId) {
        userRepository.findById(receiverId)
                .orElseThrow(() -> new RuntimeException("Receiver not found"));

        return dialogCacheService.getMessages(senderId, receiverId)
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

}
