package com.zhengwenhuan.kiwi.messaging;

import reactor.core.publisher.Sinks;

import java.util.function.Consumer;

/**
 * @author zhengwenhuan@gdmcmc.cn
 */
public class MessageConsumer implements Consumer<Sinks.Many<Message<?>>> {

    private final Consumer<Object> delegate;

    private final String input;

    public MessageConsumer(Consumer<Object> input) {
        this.delegate = input;
        this.input = null;
    }

    @Override
    public void accept(Sinks.Many<Message<?>> emitter) {
        emitter.asFlux().map(Message::payload).doOnNext(this.delegate).subscribe();
    }

    public String getInput() {
        return input;
    }
}
