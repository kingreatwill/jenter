package com.hawk.x.tracing.carrier;

import io.opentracing.propagation.TextMap;
import okhttp3.Request;

import java.util.Iterator;
import java.util.Map;

public class RequestBuilderCarrier implements TextMap {

    private Request.Builder requestBuilder;

    public RequestBuilderCarrier(Request.Builder request) {
        this.requestBuilder = request;
    }

    @Override
    public Iterator<Map.Entry<String, String>> iterator() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void put(String key, String value) {
        requestBuilder.addHeader(key, value);
    }
}
