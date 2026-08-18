package org.dialog.grpc;

import com.socialnetwork.dialog.grpc.DialogServiceGrpc;
import com.socialnetwork.dialog.grpc.ListMessagesRequest;
import com.socialnetwork.dialog.grpc.ListMessagesResponse;
import com.socialnetwork.dialog.grpc.SendMessageRequest;
import com.socialnetwork.dialog.grpc.SendMessageResponse;
import io.grpc.stub.StreamObserver;
import lombok.RequiredArgsConstructor;
import net.devh.boot.grpc.server.service.GrpcService;
import org.dialog.service.DialogService;
import org.slf4j.MDC;

@GrpcService
@RequiredArgsConstructor
public class DialogServiceImpl extends DialogServiceGrpc.DialogServiceImplBase {

    private final DialogService dialogService;

    @Override
    public void sendMessage(SendMessageRequest request, StreamObserver<SendMessageResponse> responseObserver) {
        String requestId = MDC.get("requestId");
        System.out.println(
                "Received: requestId -> " + requestId + " SenderId -> " + request.getSenderId() + " -> " + request.getReceiverId() + ": " + request.getText()
        );

        String senderId = request.getSenderId();
        String receiverId = request.getReceiverId();
        String text = request.getText();

        dialogService.sendMessage(senderId, receiverId, text);

        var response = SendMessageResponse.newBuilder()
                .setMessage("Message received")
                .build();

        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    @Override
    public void listMessages(ListMessagesRequest request, StreamObserver<ListMessagesResponse> responseObserver) {
        String requestId = MDC.get("requestId");
        System.out.println(
                "Received: requestId -> " + requestId + " SenderId -> " + request.getSenderId() + " -> " + request.getReceiverId()
        );

        var response = ListMessagesResponse.newBuilder()
                .addAllMessages(dialogService.listMessages(request.getSenderId(), request.getReceiverId()))
                .build();

        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

}
