package com.zhengwenhuan.kiwi.messaging;

import reactor.core.publisher.Sinks;

import java.util.function.Consumer;

/**
 * @author zhengwenhuan@gdmcmc.cn
 */
public class MessageConsumer implements Consumer<Sinks.Many<Message<Object>>> {

    private final Consumer<Message<Object>> delegate;

    public MessageConsumer(Consumer<Message<Object>> input) {
        this.delegate = input;
    }

    @Override
    public void accept(Sinks.Many<Message<Object>> emitter) {
        emitter.asFlux().doOnNext(delegate).subscribe();
    }

}
