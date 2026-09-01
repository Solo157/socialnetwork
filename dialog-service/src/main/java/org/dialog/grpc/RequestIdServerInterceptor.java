package org.dialog.grpc;

import io.grpc.ForwardingServerCallListener.SimpleForwardingServerCallListener;
import io.grpc.Metadata;
import io.grpc.ServerCall;
import io.grpc.ServerCallHandler;
import io.grpc.ServerInterceptor;
import net.devh.boot.grpc.server.interceptor.GrpcGlobalServerInterceptor;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;

/**
 * Перехватчик gRPC, предназначен для получения REQUEST_ID_METADATA_KEY из хедера и добавления его в MDC.
 */
@Component
@GrpcGlobalServerInterceptor
public class RequestIdServerInterceptor implements ServerInterceptor {

    public static final String REQUEST_ID_METADATA_KEY = "x-request-id";
    public static final String REQUEST_ID_MDC_KEY = "requestId";

    /**
     * Перехватывает каждый входящий gRPC вызов. Вытаскивает REQUEST_ID_METADATA_KEY и кладет в MDC.
     */
    @Override
    public <ReqT, RespT> ServerCall.Listener<ReqT> interceptCall(ServerCall<ReqT, RespT> call,
                                                                 Metadata headers,
                                                                 ServerCallHandler<ReqT, RespT> next) {

        String requestId = headers.get(Metadata.Key.of(REQUEST_ID_METADATA_KEY, Metadata.ASCII_STRING_MARSHALLER));
        if (requestId != null && !requestId.isBlank()) {
            MDC.put(REQUEST_ID_MDC_KEY, requestId);
        }

        ServerCall.Listener<ReqT> delegate = next.startCall(call, headers);

        // вынуждены использовать этот листенер, чтобы до завершения gRPC вызова данные в MDC сохранялись,
        // а при завершении вызова MDC очищался.
        return new SimpleForwardingServerCallListener<ReqT>(delegate) {
            @Override
            public void onComplete() {
                MDC.remove(REQUEST_ID_MDC_KEY);
                delegate.onComplete();
            }

            @Override
            public void onCancel() {
                MDC.remove(REQUEST_ID_MDC_KEY);
                delegate.onCancel();
            }
        };
    }

}
