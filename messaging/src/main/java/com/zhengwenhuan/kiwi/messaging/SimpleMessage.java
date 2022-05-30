package com.zhengwenhuan.kiwi.messaging;

import org.eclipse.collections.impl.factory.Maps;

import java.util.Map;
import java.util.Optional;

/**
 * @author zhengwenhuan@gdmcmc.cn
 */
public class SimpleMessage<T> implements Message<T> {

    private final String id;
    private final T payload;
    private final Map<String, String> headers = Maps.mutable.empty();

    public SimpleMessage(String id, T payload, Map<String, String> headers) {
        this.id = id;
        this.payload = payload;
        this.headers.putAll(headers);
    }

    @Override
    public String id() {
        return id;
    }

    @Override
    public T payload() {
        return payload;
    }

    @Override
    public String source() {
        return null;
    }

    @Override
    public Map<String, String> headers() {
        return Map.copyOf(headers);
    }

    @Override
    public Optional<String> header(String key) {
        return Optional.ofNullable(headers.get(key));
    }

    @Override
    public Message<T> header(String key, String value) {
        headers.putIfAbsent(key, value);

        return this;
    }

    @Override
    public Message<T> header(Map<String, String> source) {
        headers.putAll(source);

        return this;
    }

    @Override
    public String toString() {
        return "SimpleMessage{" +
                "id='" + id + '\'' +
                ", payload=" + payload +
                ", headers=" + headers +
                '}';
    }
}
