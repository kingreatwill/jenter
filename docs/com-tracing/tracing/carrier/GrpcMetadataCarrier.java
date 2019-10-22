package com.hawk.x.tracing.carrier;

import io.grpc.Metadata;
import io.opentracing.propagation.TextMap;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

public class GrpcMetadataCarrier implements TextMap {

    private Metadata headers;
    private Map<String, String> headerMap;
    public GrpcMetadataCarrier(Metadata headers) {
        this.headers = headers;
        final Set<String> headerKeys = headers.keys();
        headerMap = new HashMap<>(headerKeys.size());
        for (String key : headerKeys) {
            if (!key.endsWith(Metadata.BINARY_HEADER_SUFFIX)) {
                String value = headers.get(Metadata.Key.of(key, Metadata.ASCII_STRING_MARSHALLER));
                headerMap.put(key, value);
            }
        }
    }

    @Override
    public Iterator<Map.Entry<String, String>> iterator() {
        return this.headerMap.entrySet().iterator();
    }

    @Override
    public void put(String key, String value) {
        Metadata.Key<String> headerKey =  Metadata.Key.of(key, Metadata.ASCII_STRING_MARSHALLER);
        headers.put(headerKey, value);
    }
}
