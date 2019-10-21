package com.hawk.x.tracing.concept.grpc;

import io.grpc.*;
import io.opentracing.Scope;
import io.opentracing.Span;
import io.opentracing.SpanContext;
import io.opentracing.propagation.Format;
import io.opentracing.propagation.TextMap;
import io.opentracing.util.GlobalTracer;

import javax.annotation.Nullable;
import java.util.Iterator;
import java.util.Map;

// ClientCall拦截
public class ClientInterceptorImpl implements ClientInterceptor {

    private final SpanContext spanContext;

    public ClientInterceptorImpl(SpanContext spanContext){
        this.spanContext = spanContext;
    }

    @Override
    public <ReqT, RespT> ClientCall<ReqT, RespT> interceptCall(MethodDescriptor<ReqT, RespT> method, CallOptions callOptions, Channel next) {
        return new ForwardingClientCall.SimpleForwardingClientCall<ReqT, RespT>(next.newCall(method, callOptions)) {
            // 开始span;
            Span span = GlobalTracer.get().buildSpan("grpc.client "+method.getFullMethodName()).asChildOf(GlobalTracer.get().activeSpan()).start();
            @Override
            public void start(Listener<RespT> responseListener, Metadata headers) {
                // 注入metadata;
                GlobalTracer.get().inject(
                        span.context(),
                        Format.Builtin.HTTP_HEADERS,
                        new TextMap() {
                            @Override
                            public void put(String key, String value) {
                                Metadata.Key<String> headerKey =  Metadata.Key.of(key, Metadata.ASCII_STRING_MARSHALLER);
                                headers.put(headerKey, value);
                            }
                            @Override
                            public Iterator<Map.Entry<String, String>> iterator() {
                                throw new UnsupportedOperationException(
                                        "TextMapInjectAdapter should only be used with Tracer.inject()");
                            }
                        }
                );
                // Listener;
                Listener<RespT> tracingResponseListener = new ForwardingClientCallListener.SimpleForwardingClientCallListener<RespT>(responseListener) {
                    @Override
                    public void onClose(Status status, Metadata trailers) {
                        super.onClose(status, trailers);
                        span.finish();
                    }
                };
                super.start(tracingResponseListener, headers);
            }

            @Override
            public void cancel(@Nullable String message, @Nullable Throwable cause) {
                try (Scope ignored = GlobalTracer.get().scopeManager().activate(span)) {
                    super.cancel(message, cause);
                } finally {
                    span.finish();
                }
            }
        };
    }
}

