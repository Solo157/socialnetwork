package org.dialog.service;

import com.socialnetwork.dialog.grpc.Message;
import lombok.RequiredArgsConstructor;
import org.dialog.repository.DialogMessageEntity;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
public class DialogService {

    private final DialogCacheService dialogCacheService;

    public void sendMessage(String senderId, String receiverId, String text) {
        String dialogId = dialogCacheService.findOrCreateDialogId(senderId, receiverId);

        DialogMessageEntity message = DialogMessageEntity.builder()
                .id(UUID.randomUUID().toString())
                .dialogId(dialogId)
                .senderId(senderId)
                .receiverId(receiverId)
                .text(text)
                .createdAt(LocalDateTime.now())
                .build();

        dialogCacheService.addMessage(dialogId, message);
    }

    public List<Message> listMessages(String senderId, String receiverId) {
        return dialogCacheService.getMessages(senderId, receiverId)
                .stream()
                .map(this::toProto)
                .toList();
    }

    private Message toProto(DialogMessageEntity message) {
        return Message.newBuilder()
                .setId(message.getId())
                .setDialogId(message.getDialogId())
                .setSenderId(message.getSenderId())
                .setReceiverId(message.getReceiverId())
                .setText(message.getText())
                .setCreatedAt(message.getCreatedAt().toString())
                .build();
    }

}
