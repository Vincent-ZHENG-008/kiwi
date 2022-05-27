package com.zhengwenhuan.kiwi.messaging;

import org.eclipse.collections.impl.factory.Maps;
import org.reactivestreams.Publisher;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Sinks;

import java.util.List;
import java.util.Map;

/**
 * @author zhengwenhuan@gdmcmc.cn
 */
public class MessageDistributor implements MessageProducer, MessageRegistration {

    private final Map<String, Sinks.Many<Message<?>>> consumerRegistration = Maps.mutable.empty();

    @Override
    public void register(String destination, MessageSupplier supplier) {
        Publisher<Message<?>> messagePublisher = supplier.get();

        if (messagePublisher instanceof Mono<Message<?>>) {
            ((Mono<Message<?>>) messagePublisher).flatMap(message -> sendAndForget(supplier.getDestination(), message)).subscribe();
        } else if (messagePublisher instanceof Flux<Message<?>>) {
            ((Flux<Message<?>>) messagePublisher).flatMap(message -> sendAndForget(supplier.getDestination(), message)).subscribe();
        }
    }

    @Override
    public void register(MessageConsumer consumer) {
        String input = consumer.getInput();

        synchronized (consumerRegistration) {
            Sinks.Many<Message<?>> emitter = consumerRegistration.get(input);

            if (emitter == null) {
                emitter = Sinks.many().multicast().onBackpressureBuffer();
                consumerRegistration.put(input, emitter);
            }

            consumer.accept(emitter);
        }
    }

    @Override
    public void register(MessageFunction function) {
        String input = function.getInput();
        String output = function.getOutput();

        synchronized (consumerRegistration) {
            Sinks.Many<Message<?>> emitter = consumerRegistration.get(input);

            if (emitter == null) {
                emitter = Sinks.many().multicast().onBackpressureBuffer();
                consumerRegistration.put(input, emitter);
            }

            Publisher<Message<?>> messagePublisher = function.apply(emitter);

            if (messagePublisher instanceof Mono<Message<?>>) {
                ((Mono<Message<?>>) messagePublisher).flatMap(message -> sendAndForget(output, message)).subscribe();
            } else if (messagePublisher instanceof Flux<Message<?>>) {
                ((Flux<Message<?>>) messagePublisher).flatMap(message -> sendAndForget(output, message)).subscribe();
            }
        }
    }

    @Override
    public <T> Mono<Void> sendAndForget(String destination, Message<T> message) {
        Sinks.Many<Message<?>> emitter = consumerRegistration.get(destination);

        if (emitter == null) {
            // and forget
            return Mono.empty();
        }

        Sinks.EmitResult emitResult = emitter.tryEmitNext(message);
        if (emitResult.isFailure()) {
            // todo

        }
        return Mono.empty();
    }

}
