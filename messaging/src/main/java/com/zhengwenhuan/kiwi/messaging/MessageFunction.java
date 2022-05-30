package com.zhengwenhuan.kiwi.messaging;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Sinks;

import java.util.function.Function;

/**
 * @author zhengwenhuan@gdmcmc.cn
 */
public class MessageFunction implements Function<Sinks.Many<Message<Object>>, Flux<Message<Object>>> {

    private final Function<Flux<Message<Object>>, Flux<Object>> delegate;

    public MessageFunction(Function<Flux<Message<Object>>, Flux<Object>> delegate) {
        this.delegate = delegate;
    }

    @Override
    @SuppressWarnings("unchecked")
    public Flux<Message<Object>> apply(Sinks.Many<Message<Object>> emitter) {
        return Flux.from(delegate.apply(emitter.asFlux())).map(payload -> {
            if (payload instanceof Message) {
                return (Message<Object>) payload;
            }

            return Message.MessageBuilder.withPayload(payload).build();
        });
    }
}
