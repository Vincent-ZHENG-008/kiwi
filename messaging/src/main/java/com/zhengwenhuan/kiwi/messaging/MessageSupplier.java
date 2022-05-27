package com.zhengwenhuan.kiwi.messaging;

import org.reactivestreams.Publisher;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.function.Supplier;

/**
 * @author zhengwenhuan@gdmcmc.cn
 */
public class MessageSupplier implements Supplier<Publisher<Message<?>>> {

    private final String destination;

    private final Supplier<?> delegate;

    public MessageSupplier(String destination, Supplier<?> delegate) {
        this.destination = destination;
        this.delegate = delegate;
    }

    public String getDestination() {
        return destination;
    }

    @Override
    public Publisher<Message<?>> get() {
        if (delegate instanceof Publisher<?>) {
            return Flux.from((Publisher<?>) delegate).map(payload -> Message.MessageBuilder.withPayload(payload).build());
        }

        return Mono.just(delegate.get()).map(payload -> Message.MessageBuilder.withPayload(payload).build());
    }
}