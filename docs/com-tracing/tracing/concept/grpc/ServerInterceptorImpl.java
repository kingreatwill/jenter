package com.hawk.x.tracing.concept.grpc;

import com.hawk.x.tracing.carrier.GrpcMetadataCarrier;
import com.hawk.x.tracing.logging.Logging;
import com.hawk.x.tracing.op.NamedOf;
import io.grpc.*;
import io.opentracing.Scope;
import io.opentracing.Span;
import io.opentracing.propagation.Format;
import io.opentracing.propagation.TextMapAdapter;
import io.opentracing.util.GlobalTracer;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class ServerInterceptorImpl implements ServerInterceptor {
    @Override
    public <ReqT, RespT> ServerCall.Listener<ReqT> interceptCall(ServerCall<ReqT, RespT> call, Metadata headers, ServerCallHandler<ReqT, RespT> next) {
        // 放置锚点;
        Span span = GlobalTracer.get()
                .buildSpan(NamedOf.grpcServer(call.getMethodDescriptor().getFullMethodName()))
                //.withTag(Tags.ERROR, Boolean.TRUE)
                .asChildOf(GlobalTracer.get().extract(Format.Builtin.HTTP_HEADERS,new GrpcMetadataCarrier(headers)))
                .start();
        // 写入日志;
        Logging.of(span).log(call.getMethodDescriptor().getFullMethodName(),"","","",null);

        Context context = Context.current();
        ServerCall.Listener<ReqT> listenerWithContext =  Contexts.interceptCall(context, call, headers, next);

        return new ForwardingServerCallListener.SimpleForwardingServerCallListener<ReqT>(listenerWithContext){
            @Override
            public void onReady() {
                try (Scope ignored = GlobalTracer.get().scopeManager().activate(span)) {
                    super.onReady();
                }
            }
            @Override
            public void onMessage(ReqT message) {
                try (Scope ignored = GlobalTracer.get().scopeManager().activate(span)) {
                    super.onMessage(message);
                }
            }
            @Override
            public void onHalfClose() {
                try (Scope ignored = GlobalTracer.get().scopeManager().activate(span)) {
                    super.onHalfClose();
                }
            }
            @Override
            public void onCancel() {
                span.setTag("grpc.status","CANCELLED");
                try (Scope ignored = GlobalTracer.get().scopeManager().activate(span)) {
                    super.onCancel();
                } finally {
                    span.finish();
                }
            }
            @Override
            public void onComplete() {
                try (Scope ignored = GlobalTracer.get().scopeManager().activate(span)) {
                    super.onComplete();
                } finally {
                    span.finish();
                }
            }
        };
    }
}
