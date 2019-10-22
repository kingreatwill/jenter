package com.hawk.x.tracing.concept.grpc;

import com.hawk.x.tracing.carrier.GrpcMetadataCarrier;
import com.hawk.x.tracing.logging.Logging;
import com.hawk.x.tracing.op.NamedOf;
import io.grpc.*;
import io.opentracing.Scope;
import io.opentracing.Span;
import io.opentracing.propagation.Format;
import io.opentracing.propagation.TextMap;
import io.opentracing.tag.Tags;
import io.opentracing.util.GlobalTracer;

import javax.annotation.Nullable;
import java.util.Iterator;
import java.util.Map;

// ClientCall拦截
public class ClientInterceptorImpl implements ClientInterceptor {
    @Override
    public <ReqT, RespT> ClientCall<ReqT, RespT> interceptCall(MethodDescriptor<ReqT, RespT> method, CallOptions callOptions, Channel next) {
        return new ForwardingClientCall.SimpleForwardingClientCall<ReqT, RespT>(next.newCall(method, callOptions)) {
            // 放置锚点;
            Span span = GlobalTracer.get().buildSpan(NamedOf.grpcClient(method.getFullMethodName())).asChildOf(GlobalTracer.get().activeSpan()).start();

            @Override
            public void start(Listener<RespT> responseListener, Metadata headers) {

                // 注入metadata;
                GlobalTracer.get().inject(
                        span.context(),
                        Format.Builtin.HTTP_HEADERS,
                        new GrpcMetadataCarrier(headers)
                );
                // Listener;
                Listener<RespT> tracingResponseListener = new ForwardingClientCallListener.SimpleForwardingClientCallListener<RespT>(responseListener) {
                    @Override
                    public void onClose(Status status, Metadata trailers) {
                        super.onClose(status, trailers);
                        // 写入日志;
                        Logging.of(span).log(method.getFullMethodName(),"","",status,null);
                        if(status!=Status.OK){
                            span.setTag(Tags.ERROR,true);
                        }
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

