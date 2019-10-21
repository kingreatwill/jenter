package com.hawk.x.tracing.concept.https;

import com.hawk.x.tracing.carrier.RequestBuilderCarrier;
import io.opentracing.Span;
import io.opentracing.propagation.Format;
import io.opentracing.util.GlobalTracer;
import okhttp3.Request;

public class TracingHttps {
    public Span startSpan(){
        return GlobalTracer.get().buildSpan("http.client").asChildOf(GlobalTracer.get().activeSpan()).start();
    }

    public void setHeaders(Span span, Request.Builder request){
        GlobalTracer.get().inject(span.context(), Format.Builtin.HTTP_HEADERS, new RequestBuilderCarrier(request));
    }
}
