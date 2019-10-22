package com.hawk.x.tracing.concept.cache;

import io.opentracing.contrib.redis.common.TracingConfiguration;
import io.opentracing.util.GlobalTracer;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;

public class TracingJedisConnectionFactory extends JedisConnectionFactory {

    private final RedisConnectionFactory delegate;
    private final TracingConfiguration tracingConfiguration;

    public TracingJedisConnectionFactory(RedisConnectionFactory delegate) {
        this.delegate = delegate;
        this.tracingConfiguration = new TracingConfiguration.Builder(GlobalTracer.get()).traceWithActiveSpanOnly(false).build();
    }

    @Override
    public RedisConnection getConnection() {
        return new TracingRedisConnection(delegate.getConnection(), tracingConfiguration);
    }
}
