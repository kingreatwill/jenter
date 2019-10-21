package com.hawk.x.tracing.concept.grpc;

import io.grpc.*;
import io.opentracing.Scope;
import io.opentracing.Span;
import io.opentracing.SpanContext;
import io.opentracing.propagation.Format;
import io.opentracing.propagation.TextMapAdapter;
import io.opentracing.tag.Tags;
import io.opentracing.util.GlobalTracer;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class ServerInterceptorImpl implements ServerInterceptor {
    //Span span = ServerInterceptorImpl.TRACING_SPAN.get();
    public static final Context.Key<Span> TRACING_SPAN = Context.key("io.opentracing.active-span");
    public static final Context.Key<SpanContext> TRACING_SPANCONTEXT = Context.key("io.opentracing.active-span-context");
    @Override
    public <ReqT, RespT> ServerCall.Listener<ReqT> interceptCall(ServerCall<ReqT, RespT> call, Metadata headers, ServerCallHandler<ReqT, RespT> next) {

        final Set<String> headerKeys = headers.keys();
        Map<String, String> headerMap = new HashMap<>(headerKeys.size());
        for (String key : headerKeys) {
            if (!key.endsWith(Metadata.BINARY_HEADER_SUFFIX)) {
                String value = headers.get(Metadata.Key.of(key, Metadata.ASCII_STRING_MARSHALLER));
                headerMap.put(key, value);
            }
        }

        Span span = GlobalTracer.get()
                .buildSpan("grpc.server "+call.getMethodDescriptor().getFullMethodName())
                .withTag(Tags.SPAN_KIND.getKey(), Tags.SPAN_KIND_SERVER)
                //.withTag(Tags.ERROR, Boolean.TRUE)
                .asChildOf(GlobalTracer.get().extract(Format.Builtin.HTTP_HEADERS, new TextMapAdapter(headerMap)))
                .start();


        Context context = Context.current().withValue(TRACING_SPAN, span).withValue(TRACING_SPANCONTEXT, span.context());

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
