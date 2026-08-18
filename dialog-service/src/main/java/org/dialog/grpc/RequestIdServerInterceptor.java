package org.dialog.grpc;

import io.grpc.ForwardingServerCall.SimpleForwardingServerCall;
import io.grpc.Metadata;
import io.grpc.ServerCall;
import io.grpc.ServerCallHandler;
import io.grpc.ServerInterceptor;
import net.devh.boot.grpc.server.interceptor.GrpcGlobalServerInterceptor;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;

@Component
@GrpcGlobalServerInterceptor
public class RequestIdServerInterceptor implements ServerInterceptor {

    public static final String REQUEST_ID_METADATA_KEY = "x-request-id";

    public static final String REQUEST_ID_MDC_KEY = "requestId";

    @Override
    public <ReqT, RespT> ServerCall.Listener<ReqT> interceptCall(
            ServerCall<ReqT, RespT> call,
            Metadata headers,
            ServerCallHandler<ReqT, RespT> next) {

        String requestId = headers.get(
                Metadata.Key.of(REQUEST_ID_METADATA_KEY, Metadata.ASCII_STRING_MARSHALLER)
        );
        if (requestId != null && !requestId.isBlank()) {
            MDC.put(REQUEST_ID_MDC_KEY, requestId);
        }

        ServerCall.Listener<ReqT> listener = next.startCall(
                new SimpleForwardingServerCall<>(call) {
                    @Override
                    public void sendHeaders(Metadata headers) {
                        if (requestId != null && !requestId.isBlank()) {
                            headers.put(
                                    Metadata.Key.of(REQUEST_ID_METADATA_KEY, Metadata.ASCII_STRING_MARSHALLER),
                                    requestId
                            );
                        }
                        super.sendHeaders(headers);
                    }
                },
                headers
        );

        return new ServerCall.Listener<>() {
            @Override
            public void onReady() {
                listener.onReady();
            }

            @Override
            public void onMessage(ReqT message) {
                listener.onMessage(message);
            }

            @Override
            public void onHalfClose() {
                listener.onHalfClose();
            }

            @Override
            public void onCancel() {
                MDC.remove(REQUEST_ID_MDC_KEY);
                listener.onCancel();
            }

            @Override
            public void onComplete() {
                MDC.remove(REQUEST_ID_MDC_KEY);
                listener.onComplete();
            }
        };
    }

}
