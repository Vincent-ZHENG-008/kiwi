package com.zhengwenhuan.kiwi.messaging;

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

    class MessageBuilder<T> {

        private final T payload;

        public MessageBuilder(T payload) {
            this.payload = payload;
        }

        public Message<T> build() {
            return new SimpleMessage<>("", payload);
        }

        public static <T> MessageBuilder<T> withPayload(T payload) {
            return new MessageBuilder<>(payload);
        }

    }

}
