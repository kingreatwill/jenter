package com.tracing;

import com.tracing.concept.amqp.TracingAmqp;

public class Tracing {
    public static TracingAmqp amqp() {
        return new TracingAmqp();
    }
}
