package com.hawk.x.tracing.concept.amqp;

import com.hawk.x.tracing.carrier.RabbitMqCarrier;
import io.opentracing.Span;
import io.opentracing.SpanContext;
import io.opentracing.propagation.Format;
import io.opentracing.util.GlobalTracer;
import org.springframework.amqp.core.MessageProperties;

import java.util.Map;

public class TracingAmqp {
    public Span StartSpanFromContext(Span span){
        return GlobalTracer.get().buildSpan("Pub").asChildOf(span).start();
    }

    public void SetHeaders(Span span, MessageProperties messageProperties){
        GlobalTracer.get().inject(span.context(), Format.Builtin.TEXT_MAP,new RabbitMqCarrier(messageProperties));
        // Message msg = template.getMessageConverter().toMessage(Jsons.marshalObject(topic.getMessage()),new MessageProperties());
    }

    public void StartSpanFromDelivery(org.springframework.amqp.core.Message message){
        Map<String, Object> headers = message.getMessageProperties().getHeaders();
        SpanContext spanContext = GlobalTracer.get().extract(Format.Builtin.TEXT_MAP, new RabbitMqCarrier(headers));
        GlobalTracer.get().buildSpan("C").asChildOf(spanContext).start();
    }
}
