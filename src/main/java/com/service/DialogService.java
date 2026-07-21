package com.service;

import com.api.DialogMessageResponse;
import com.database.DialogEntity;
import com.database.DialogMessageEntity;
import com.database.DialogMessageRepository;
import com.database.DialogRepository;
import com.database.UserRepository;
import com.dto.SendDialogMessageRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class DialogService {

    private final DialogMessageRepository dialogMessageRepository;
    private final DialogRepository dialogRepository;
    private final UserRepository userRepository;

    @Transactional(readOnly = false)
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

        dialogMessageRepository.save(message);
    }

    @Transactional(readOnly = false)
    public List<DialogMessageResponse> listMessages(String senderId, String receiverId) {
        userRepository.findById(receiverId)
                .orElseThrow(() -> new RuntimeException("Receiver not found"));

        String dialogId = findDialogId(senderId, receiverId);
        if (dialogId == null) {
            return List.of();
        }

        return dialogMessageRepository.findByDialogId(dialogId)
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
        return dialogRepository.findByParticipants(userId1, userId2)
                .map(DialogEntity::getId)
                .orElseGet(() -> {
                    String id = UUID.randomUUID().toString();
                    DialogEntity dialog = DialogEntity.builder()
                            .id(id)
                            .user1Id(userId1)
                            .user2Id(userId2)
                            .createdAt(LocalDateTime.now())
                            .build();
                    dialogRepository.save(dialog);
                    return id;
                });
    }

    private String findDialogId(String userId1, String userId2) {
        return dialogRepository.findByParticipants(userId1, userId2)
                .map(DialogEntity::getId)
                .orElse(null);
    }

}
