package com.hawk.x.tracing.concept.amqp;

import com.hawk.x.tracing.carrier.RabbitMqMessagePropertiesCarrier;
import com.hawk.x.tracing.logging.Logging;
import com.hawk.x.tracing.op.NamedOf;
import io.opentracing.Span;
import io.opentracing.SpanContext;
import io.opentracing.propagation.Format;
import io.opentracing.util.GlobalTracer;
import org.springframework.amqp.core.MessageProperties;

import java.util.Map;

public class TracingAmqp {

    // (Amqp话题生产者)放置锚点;
    public Span startSpan(){
        return GlobalTracer.get()
                .buildSpan(NamedOf.amqpTopicProducer())
                .asChildOf(GlobalTracer.get().activeSpan())
                .start();
    }

    // (Amqp话题生产者)注入头部;
    public void setHeaders(Span span, MessageProperties messageProperties){
        // 注入头部(不放置锚点);
        GlobalTracer.get().inject(span.context(), Format.Builtin.TEXT_MAP,new RabbitMqMessagePropertiesCarrier(messageProperties));
        // 写入日志;
        Logging.of(span).log("","amqp.publishing","","",null);
    }

    // (Amqp队列消费者)放置锚点;
    public Span startSpanFromDelivery(org.springframework.amqp.core.Message message){
        // 提取锚点上下文;
        SpanContext spanContext = GlobalTracer.get().extract(Format.Builtin.TEXT_MAP, new RabbitMqMessagePropertiesCarrier(message.getMessageProperties()));
        // 放置锚点;
        Span span = GlobalTracer.get().buildSpan(NamedOf.amqpConsumer("")).asChildOf(spanContext).start();
        // 写入日志;
        Logging.of(span).log("","amqp.consume","","",null, message.toString());
        return span;
    }
}
