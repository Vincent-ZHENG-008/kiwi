package com.zhengwenhuan.kiwi.messaging;

import org.reactivestreams.Publisher;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Sinks;
import reactor.core.scheduler.Schedulers;

import java.util.function.Function;

/**
 * @author zhengwenhuan@gdmcmc.cn
 */
public class MessageFunction implements Function<Sinks.Many<Message<?>>, Publisher<Message<?>>> {

    private final Function<Publisher<?>, Publisher<Message<?>>> delegate;
    private final String input;
    private final String output;

    public MessageFunction(Function<Publisher<?>, Publisher<Message<?>>> delegate) {
        this.delegate = delegate;
        this.input = null;
        this.output = null;
    }

    @Override
    public Publisher<Message<?>> apply(Sinks.Many<Message<?>> emitter) {
        Publisher<?> apply = delegate.apply(emitter.asFlux().map(Message::payload));

        return Flux.from(apply).map(payload -> Message.MessageBuilder.withPayload(payload).build());
    }

    public String getInput() {
        return input;
    }

    public String getOutput() {
        return output;
    }
}
