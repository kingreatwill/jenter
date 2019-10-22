package com.hawk.x.tracing.op;

public class NamedOf {
    public static String amqpTopicProducer(){
        return String.format("amqp.topic.producer");
    }

    public static String amqpConsumer(String queue){
        return String.format("amqp.consumer %s", queue);
    }

    public static String httpdServer(String method, String path){
        return String.format("httpd.server %s %s", method, path);
    }

    public static String httpsClient(String method, String path){
        return String.format("https.client %s %s", method, path);
    }

    public static String grpcServer(String method){
        return String.format("grpc.server %s", method);
    }

    public static String grpcClient(String method){
        return String.format("grpc.client %s", method);
    }

    public static String sql(String method){
        return String.format("sql %s", method);
    }
}
