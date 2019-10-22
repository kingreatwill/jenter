package com.tracing;

import io.jaegertracing.Configuration;
import io.jaegertracing.internal.samplers.ConstSampler;
import io.opentracing.util.GlobalTracer;

public class XTracer {
    // 启动(调试);
    public void Debug(){
        Start("tracing.debug");
    }

    // 启动;
    public void Start(String named){
        io.opentracing.Tracer tracer = new Configuration(named)
                .withReporter(
                        new Configuration.ReporterConfiguration()
                                .withLogSpans(true)
                                .withFlushInterval(2*1000)
                                .withMaxQueueSize(10000)
                                .withSender(
                                        new Configuration.SenderConfiguration()
                                                .withAgentHost("127.0.0.1")
                                                .withAgentPort(6831)
                                                .withEndpoint("")
                                )
                ).withSampler(
                        new Configuration.SamplerConfiguration()
                                .withType(ConstSampler.TYPE)
                                .withParam(1)
                ).getTracer();
        // 单例(全局);
        GlobalTracer.registerIfAbsent(tracer);
    }
}
