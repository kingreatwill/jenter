package com.hawk.x.tracing;

import com.hawk.x.configs.cfg.Tracing;
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
                        .withLogSpans(Tracing.getInstance().getLogSpans())
                        .withFlushInterval(Tracing.getInstance().getBufferFlushInterval()*1000)
                        .withMaxQueueSize(Tracing.getInstance().getQueueSize())
                        .withSender(
                                new Configuration.SenderConfiguration()
                                .withAgentHost(Tracing.getInstance().getAgentHost())
                                .withAgentPort(Tracing.getInstance().getAgentPort())
                                .withEndpoint(Tracing.getInstance().getCollectorEndpoint())
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
