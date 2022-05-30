package com.zhengwenhuan.kiwi.messaging;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * @author zhengwenhuan@gdmcmc.cn
 */
public interface Message<T> {

    String id();

    T payload();

    String source();

    Map<String, String> headers();

    Optional<String> header(String key);

    Message<T> header(String key, String value);

    Message<T> header(Map<String, String> source);

    class MessageBuilder<T> {

        private final T payload;

        private final Map<String, String> headers = new HashMap<>();

        public MessageBuilder(T payload) {
            this.payload = payload;
        }

        public MessageBuilder<T> withHeader(Map<String, String> headers) {
            this.headers.putAll(headers);

            return this;
        }

        public Message<T> build() {
            return new SimpleMessage<>("", payload, headers);
        }

        public static <T> MessageBuilder<T> withPayload(T payload) {
            return new MessageBuilder<>(payload);
        }

    }

}
