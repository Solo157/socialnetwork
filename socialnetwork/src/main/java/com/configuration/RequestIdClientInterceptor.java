package com.configuration;

import io.grpc.CallOptions;
import io.grpc.Channel;
import io.grpc.ClientCall;
import io.grpc.ClientInterceptor;
import io.grpc.ForwardingClientCall.SimpleForwardingClientCall;
import io.grpc.Metadata;
import io.grpc.MethodDescriptor;
import net.devh.boot.grpc.client.interceptor.GrpcGlobalClientInterceptor;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;

@Component
@GrpcGlobalClientInterceptor
public class RequestIdClientInterceptor implements ClientInterceptor {

    public static final String REQUEST_ID_METADATA_KEY = "x-request-id";

    @Override
    public <ReqT, RespT> ClientCall<ReqT, RespT> interceptCall(
            MethodDescriptor<ReqT, RespT> method,
            CallOptions callOptions,
            Channel next) {

        return new SimpleForwardingClientCall<>(next.newCall(method, callOptions)) {

            @Override
            public void start(Listener<RespT> responseListener, Metadata headers) {
                String requestId = MDC.get(RequestIdFilter.REQUEST_ID_MDC_KEY);
                if (requestId != null && !requestId.isBlank()) {
                    headers.put(
                            Metadata.Key.of(REQUEST_ID_METADATA_KEY, Metadata.ASCII_STRING_MARSHALLER),
                            requestId
                    );
                }
                super.start(responseListener, headers);
            }
        };
    }

}
