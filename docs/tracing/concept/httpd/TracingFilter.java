package com.hawk.x.tracing.concept.httpd;

import com.hawk.x.tracing.carrier.HttpServletRequestCarrier;
import io.opentracing.Scope;
import io.opentracing.Span;
import io.opentracing.SpanContext;
import io.opentracing.propagation.Format;
import io.opentracing.tag.Tags;
import io.opentracing.util.GlobalTracer;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

// 调用链;
@Configuration
public class TracingFilter implements Filter {

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        // 当前请求响应;
        HttpServletRequest request = (HttpServletRequest) servletRequest;
        HttpServletResponse response = (HttpServletResponse) servletResponse;
        // 自定义Header;
        this._tracing(request,response,filterChain);
    }

    // 自定义Header;
    private void _tracing(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws IOException, ServletException {
        // 提取;
        SpanContext extractedContext = GlobalTracer.get().extract(Format.Builtin.HTTP_HEADERS,  new HttpServletRequestCarrier(request));
        final Span span = GlobalTracer.get().buildSpan(request.getMethod()+" "+request.getRequestURI())
                .asChildOf(extractedContext)
                .withTag(Tags.SPAN_KIND.getKey(), Tags.SPAN_KIND_SERVER)
                .start();
        Map logs = new HashMap<String,String>();
        logs.put("http.query",request.getQueryString());
        span.log(logs);
        // 设置span;
        request.setAttribute(Utils.SERVER_SPAN_CONTEXT, span.context());
        try (Scope scope = GlobalTracer.get().activateSpan(span)) {
            // 输出span, 方便追查;
            response.addHeader("Span",span.toString());
            filterChain.doFilter(request, response);
        } catch (Exception e) {
            throw e;
        } finally {
            span.finish();
        }
    }
}

class Utils {
    public static final String SERVER_SPAN_CONTEXT = "io.opentracing.activeSpanContext";
    public static SpanContext SpanContext() {
        return (SpanContext)((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest().getAttribute(SERVER_SPAN_CONTEXT);
    }
}