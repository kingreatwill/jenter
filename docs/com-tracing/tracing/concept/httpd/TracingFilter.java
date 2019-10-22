package com.hawk.x.tracing.concept.httpd;

import com.hawk.x.tracing.carrier.HttpServletRequestCarrier;
import com.hawk.x.tracing.logging.Logging;
import com.hawk.x.tracing.op.NamedOf;
import io.opentracing.Scope;
import io.opentracing.Span;
import io.opentracing.SpanContext;
import io.opentracing.propagation.Format;
import io.opentracing.tag.Tags;
import io.opentracing.util.GlobalTracer;
import org.springframework.context.annotation.Configuration;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

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

    // 追踪;
    private void _tracing(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws IOException, ServletException {
        // 提取锚点上下文;
        SpanContext extractedContext = GlobalTracer.get().extract(Format.Builtin.HTTP_HEADERS,  new HttpServletRequestCarrier(request));
        // 放置锚点;
        final Span span = GlobalTracer.get().buildSpan(NamedOf.httpdServer(request.getMethod(),request.getRequestURI()))
                .asChildOf(extractedContext)
                .withTag(Tags.SPAN_KIND.getKey(), Tags.SPAN_KIND_SERVER)
                .start();
        // 写入日志;
        Logging.of(span).log(request.getRequestURI(),request.getMethod(),request.getQueryString(),response.getStatus(),null);
        // 激活锚点;
        try (Scope scope = GlobalTracer.get().activateSpan(span)) {
            // 设置头部(以便追查报文);
            response.addHeader("Tracing-Span",span.toString());
            filterChain.doFilter(request, response);
        } catch (Exception e) {
            throw e;
        } finally {
            span.finish();
        }
    }
}