package com.zhengwenhuan.kiwi.messaging;

import org.eclipse.collections.impl.factory.Maps;
import org.reactivestreams.Publisher;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Sinks;

import java.util.Map;

/**
 * @author zhengwenhuan@gdmcmc.cn
 */
public class MessageDistributor implements MessageProducer, MessageRegistration {

    private final Map<String, Sinks.Many<Message<?>>> consumerRegistration = Maps.mutable.empty();

    @Override
    public void register(String outgoing, MessageSupplier supplier) {
        synchronized (consumerRegistration) {
            Publisher<Message<?>> messagePublisher = supplier.get();

            if (messagePublisher instanceof Mono<Message<?>>) {
                ((Mono<Message<?>>) messagePublisher).flatMap(message -> sendAndForget(outgoing, message)).subscribe();
            } else if (messagePublisher instanceof Flux<Message<?>>) {
                ((Flux<Message<?>>) messagePublisher).flatMap(message -> sendAndForget(outgoing, message)).subscribe();
            }
        }
    }

    @Override
    public void register(String incoming, MessageConsumer consumer) {
        synchronized (consumerRegistration) {
            Sinks.Many<Message<?>> emitter = consumerRegistration.get(incoming);

            if (emitter == null) {
                emitter = Sinks.many().multicast().onBackpressureBuffer();
                consumerRegistration.put(incoming, emitter);
            }

            consumer.accept(emitter);
        }
    }

    @Override
    public void register(String incoming, String outgoing, MessageFunction function) {
        synchronized (consumerRegistration) {
            Sinks.Many<Message<?>> emitter = consumerRegistration.get(incoming);

            if (emitter == null) {
                emitter = Sinks.many().multicast().onBackpressureBuffer();
                consumerRegistration.put(incoming, emitter);
            }

            Publisher<Message<?>> messagePublisher = function.apply(emitter);

            if (messagePublisher instanceof Mono<Message<?>>) {
                ((Mono<Message<?>>) messagePublisher).flatMap(message -> sendAndForget(outgoing, message)).subscribe();
            } else if (messagePublisher instanceof Flux<Message<?>>) {
                ((Flux<Message<?>>) messagePublisher).flatMap(message -> sendAndForget(outgoing, message)).subscribe();
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
