package org.counter.grpc;

import com.socialnetwork.dialog.grpc.DialogServiceGrpc;
import com.socialnetwork.dialog.grpc.ListMessagesRequest;
import com.socialnetwork.dialog.grpc.ListMessagesResponse;
import com.socialnetwork.dialog.grpc.SendMessageRequest;
import com.socialnetwork.dialog.grpc.SendMessageResponse;
import io.grpc.stub.StreamObserver;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.server.service.GrpcService;
import org.counter.handler.DialogServiceHandler;
import org.slf4j.MDC;

@Slf4j
@GrpcService
@RequiredArgsConstructor
public class DialogServiceImpl extends DialogServiceGrpc.DialogServiceImplBase {

    private final DialogServiceHandler dialogService;

    @Override
    public void sendMessage(SendMessageRequest request, StreamObserver<SendMessageResponse> responseObserver) {
        String requestId = MDC.get("requestId");
        log.info("Received: requestId -> {} SenderId -> {} -> {}: {}",
                requestId, request.getSenderId(), request.getReceiverId(), request.getText());

        String senderId = request.getSenderId();
        String receiverId = request.getReceiverId();
        String text = request.getText();

        dialogService.sendMessage(senderId, receiverId, text);

        var response = SendMessageResponse.newBuilder()
                .setMessage("Message received")
                .build();

        // отдаем ответ и завершаем gRPC вызов
        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    @Override
    public void listMessages(ListMessagesRequest request, StreamObserver<ListMessagesResponse> responseObserver) {
        String requestId = MDC.get("requestId");
        log.info("Received: requestId -> {} SenderId -> {} -> {}",
                requestId, request.getSenderId(), request.getReceiverId());

        var response = ListMessagesResponse.newBuilder()
                .addAllMessages(dialogService.listMessages(request.getSenderId(), request.getReceiverId()))
                .build();

        // отдаем ответ и завершаем gRPC вызов
        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

}
