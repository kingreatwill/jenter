package com.hawk.x.tracing.carrier;

import io.opentracing.propagation.TextMap;
import javax.servlet.http.HttpServletRequest;
import java.util.*;

public class HttpServletRequestCarrier implements TextMap {

    private Map<String, List<String>> headers;

    public HttpServletRequestCarrier(HttpServletRequest httpServletRequest) {
        headers = servletHeadersToMultiMap(httpServletRequest);
    }

    @Override
    public Iterator<Map.Entry<String, String>> iterator() {
        return new MultivaluedMapFlatIterator<>(headers.entrySet());
    }

    @Override
    public void put(String key, String value) {
        throw new UnsupportedOperationException();
    }

    protected Map<String, List<String>> servletHeadersToMultiMap(HttpServletRequest httpServletRequest) {
        Map<String, List<String>> headersResult = new HashMap<>();

        Enumeration<String> headerNamesIt = httpServletRequest.getHeaderNames();
        while (headerNamesIt.hasMoreElements()) {
            String headerName = headerNamesIt.nextElement();

            Enumeration<String> valuesIt = httpServletRequest.getHeaders(headerName);
            List<String> valuesList = new ArrayList<>(1);
            while (valuesIt.hasMoreElements()) {
                valuesList.add(valuesIt.nextElement());
            }

            headersResult.put(headerName, valuesList);
        }

        return headersResult;
    }

    public static final class MultivaluedMapFlatIterator<K, V> implements Iterator<Map.Entry<K, V>> {

        private final Iterator<Map.Entry<K, List<V>>> mapIterator;
        private Map.Entry<K, List<V>> mapEntry;
        private Iterator<V> listIterator;

        public MultivaluedMapFlatIterator(Set<Map.Entry<K, List<V>>> multiValuesEntrySet) {
            this.mapIterator = multiValuesEntrySet.iterator();
        }

        @Override
        public boolean hasNext() {
            if (listIterator != null && listIterator.hasNext()) {
                return true;
            }

            return mapIterator.hasNext();
        }

        @Override
        public Map.Entry<K, V> next() {
            if (mapEntry == null || (!listIterator.hasNext() && mapIterator.hasNext())) {
                mapEntry = mapIterator.next();
                listIterator = mapEntry.getValue().iterator();
            }

            if (listIterator.hasNext()) {
                return new AbstractMap.SimpleImmutableEntry<>(mapEntry.getKey(), listIterator.next());
            } else {
                return new AbstractMap.SimpleImmutableEntry<>(mapEntry.getKey(), null);
            }
        }

        @Override
        public void remove() {
            throw new UnsupportedOperationException();
        }
    }
}
