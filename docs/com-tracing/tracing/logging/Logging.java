package com.hawk.x.tracing.logging;

import io.opentracing.Span;
import sun.util.logging.resources.logging;

import java.util.HashMap;
import java.util.Map;

public class Logging {

    final Span span;

    public Logging(Span span) {
        this.span = span;
    }

    // 构建;
    public static Logging of(Span span){
        return new Logging(span);
    }

    // 写入;
    public void log(String route, String method, String scenes, Object stats, Exception err, Object... args) {
        Map logs = new HashMap<String,Object>();
        logs.put("route",route);
        logs.put("method",method);
        logs.put("scenes",scenes);
        logs.put("args",args);
        logs.put("stats",stats);
        logs.put("error",err);
        this.span.log(logs);
    }
}
