package com.zhengwenhuan.kiwi.messaging;

import org.reactivestreams.Publisher;
import reactor.core.publisher.Flux;

import java.util.function.Supplier;

/**
 * @author zhengwenhuan@gdmcmc.cn
 */
public class MessageSupplier implements Supplier<Publisher<Message<?>>> {

    private final Supplier<Publisher<?>> delegate;

    public MessageSupplier(Supplier<Publisher<?>> delegate) {
        this.delegate = delegate;
    }

    @Override
    public Publisher<Message<?>> get() {
        return Flux.from((Publisher<?>) delegate).map(payload -> Message.MessageBuilder.withPayload(payload).build());
    }
}
