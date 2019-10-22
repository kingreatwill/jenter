package com.hawk.x.tracing.carrier;

import io.opentracing.propagation.TextMap;
import org.springframework.amqp.core.MessageProperties;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class RabbitMqMessagePropertiesCarrier implements TextMap {

    private MessageProperties messageProperties;
    private Map<String, String> map = new HashMap<>();

    public RabbitMqMessagePropertiesCarrier(MessageProperties messageProperties) {
        this.messageProperties = messageProperties;
        messageProperties.getHeaders().forEach(
                (key, value) -> {
                    if (value == null) {
                        return;
                    }
                    map.put(key, value.toString());
                });
    }

    @Override
    public void put(String key, String value) {
        messageProperties.getHeaders().put(key, value);
    }

    @Override
    public Iterator<Map.Entry<String, String>> iterator() {
        return map.entrySet().iterator();
    }
}
