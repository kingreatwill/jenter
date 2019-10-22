package com.hawk.x.tracing.concept.https;

import com.hawk.x.tracing.carrier.Okhttp3RequestBuilderCarrier;
import com.hawk.x.tracing.logging.Logging;
import com.hawk.x.tracing.op.NamedOf;
import io.opentracing.Span;
import io.opentracing.propagation.Format;
import io.opentracing.util.GlobalTracer;
import okhttp3.Request;

public class TracingHttps {
    public Span startSpan(String method,String path){
        // 放置锚点;
        Span span = GlobalTracer.get().buildSpan(NamedOf.httpsClient(method,path)).asChildOf(GlobalTracer.get().activeSpan()).start();
        // 写入日志;
        Logging.of(span).log(path,method,"","",null);
        return span;
    }

    public void setHeaders(Span span, Request.Builder request){
        // 注入头部(不放置锚点);
        GlobalTracer.get().inject(span.context(), Format.Builtin.HTTP_HEADERS, new Okhttp3RequestBuilderCarrier(request));
    }
}
