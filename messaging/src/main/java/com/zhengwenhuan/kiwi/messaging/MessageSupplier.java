package com.zhengwenhuan.kiwi.messaging;

import reactor.core.publisher.Flux;

import java.util.function.Supplier;

/**
 * @author zhengwenhuan@gdmcmc.cn
 */
public class MessageSupplier implements Supplier<Flux<Message<Object>>> {

    private final Supplier<Flux<Object>> delegate;

    public MessageSupplier(Supplier<Flux<Object>> delegate) {
        this.delegate = delegate;
    }

    @Override
    public Flux<Message<Object>> get() {
        return Flux.from(delegate.get()).map(payload -> Message.MessageBuilder.withPayload(payload).build());
    }
}
