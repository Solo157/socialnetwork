package com.service;

import com.api.DialogMessageResponse;
import com.database.UserRepository;
import com.dto.SendDialogMessageRequest;
import com.socialnetwork.dialog.grpc.DialogServiceGrpc;
import com.socialnetwork.dialog.grpc.ListMessagesRequest;
import com.socialnetwork.dialog.grpc.ListMessagesResponse;
import com.socialnetwork.dialog.grpc.Message;
import com.socialnetwork.dialog.grpc.SendMessageRequest;
import com.socialnetwork.dialog.grpc.SendMessageResponse;
import lombok.RequiredArgsConstructor;
import net.devh.boot.grpc.client.inject.GrpcClient;
import org.slf4j.MDC;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class DialogService {

    private final UserRepository userRepository;

    @GrpcClient("dialogService")
    private DialogServiceGrpc.DialogServiceBlockingStub stub;

    public void sendMessage(String senderId, String receiverId, SendDialogMessageRequest request) {
        userRepository.findById(receiverId)
                .orElseThrow(() -> new RuntimeException("Receiver not found"));

        String requestId = MDC.get("requestId");
        System.out.println(
                "Received: requestId -> " + requestId + " SenderId -> " + senderId + " -> " + receiverId + ": " + request.getText()
        );

        SendMessageRequest grpcRequest = SendMessageRequest.newBuilder()
                .setSenderId(senderId)
                .setReceiverId(receiverId)
                .setText(request.getText())
                .build();

        SendMessageResponse response = stub.sendMessage(grpcRequest);

        System.out.println("gRPC response: " + response.getMessage());
    }

    public List<DialogMessageResponse> listMessages(String senderId, String receiverId) {
        userRepository.findById(receiverId)
                .orElseThrow(() -> new RuntimeException("Receiver not found"));

        String requestId = MDC.get("requestId");
        System.out.println("Received: requestId -> " + requestId + " SenderId -> " + senderId + " -> " + receiverId);

        ListMessagesResponse response = stub.listMessages(
                ListMessagesRequest.newBuilder()
                        .setSenderId(senderId)
                        .setReceiverId(receiverId)
                        .build()
        );

        return response.getMessagesList()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    private DialogMessageResponse toResponse(Message message) {
        return DialogMessageResponse.builder()
                .id(message.getId())
                .dialogId(message.getDialogId())
                .senderId(message.getSenderId())
                .receiverId(message.getReceiverId())
                .text(message.getText())
                .createdAt(LocalDateTime.parse(message.getCreatedAt()))
                .build();
    }

}
