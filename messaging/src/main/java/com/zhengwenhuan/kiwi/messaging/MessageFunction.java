package com.zhengwenhuan.kiwi.messaging;

import org.reactivestreams.Publisher;
import reactor.core.CorePublisher;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Sinks;

import java.util.function.Function;

/**
 * @author zhengwenhuan@gdmcmc.cn
 */
public class MessageFunction implements Function<Sinks.Many<Message<?>>, Publisher<Message<?>>> {

    private final Function<CorePublisher<?>, Publisher<?>> delegate;

    public MessageFunction(Function<CorePublisher<?>, Publisher<?>> delegate) {
        this.delegate = delegate;
    }

    @Override
    public Publisher<Message<?>> apply(Sinks.Many<Message<?>> emitter) {
        Publisher<?> apply = delegate.apply(emitter.asFlux().map(Message::payload));

        return Flux.from(apply).map(payload -> Message.MessageBuilder.withPayload(payload).build());
    }
}
