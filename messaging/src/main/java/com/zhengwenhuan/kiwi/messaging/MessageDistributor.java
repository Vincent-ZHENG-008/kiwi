package com.zhengwenhuan.kiwi.messaging;

import org.eclipse.collections.impl.factory.Maps;
import org.reactivestreams.Publisher;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Sinks;

import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * @author zhengwenhuan@gdmcmc.cn
 */
public class MessageDistributor implements MessageProducer, MessageRegistration {

    private static final String DLQ_DEFAULT = "DLQ_Default";

    private final Map<String, Sinks.Many<Message<?>>> consumerRegistration = Maps.mutable.empty();
    private final ReadWriteLock rwLock = new ReentrantReadWriteLock();
    private final Lock rLock = rwLock.readLock();
    private final Lock wLock = rwLock.writeLock();

    public MessageDistributor() {
        this(false, null);
    }

    public MessageDistributor(boolean dlqDefault, MessageConsumer dlqConsumer) {
        // open default dlq
        if (dlqDefault) {
            try (TerminateExecute ignored = new TerminateExecute(wLock::unlock)) {
                // get lock
                tryGetWriteLock(wLock);

                if (dlqConsumer == null) {
                    dlqConsumer = new DLQMessageConsumer();
                }
                Sinks.Many<Message<?>> emitter = consumerRegistration.get(DLQ_DEFAULT);
                if (emitter == null) {
                    emitter = Sinks.many().multicast().onBackpressureBuffer();
                    consumerRegistration.put(DLQ_DEFAULT, emitter);
                }
                dlqConsumer.accept(emitter);
            }
        }
    }

    @Override
    public void register(String outgoing, MessageSupplier supplier) {
        try (TerminateExecute ignored = new TerminateExecute(rLock::unlock)) {
            rLock.lock();

            Publisher<Message<?>> messagePublisher = supplier.get();

            messageChannelSubscribe(messagePublisher, outgoing);
        }
    }

    @Override
    public void register(String incoming, MessageConsumer consumer) {
        try (TerminateExecute ignored = new TerminateExecute(wLock::unlock)) {
            // get lock
            tryGetWriteLock(wLock);

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
        try (TerminateExecute ignored = new TerminateExecute(wLock::unlock)) {
            // get lock
            tryGetWriteLock(wLock);

            Sinks.Many<Message<?>> emitter = consumerRegistration.get(incoming);
            if (emitter == null) {
                emitter = Sinks.many().multicast().onBackpressureBuffer();
                consumerRegistration.put(incoming, emitter);
            }

            Publisher<Message<?>> messagePublisher = function.apply(emitter);
            messageChannelSubscribe(messagePublisher, outgoing);
        }
    }

    @Override
    public <T> Mono<Void> sendAndForget(String destination, Message<T> message) {
        Sinks.Many<Message<?>> emitter = consumerRegistration.get(destination);
        if (emitter == null) {
            return Mono.error(new NullPointerException("not match destination with :" + destination));
        }

        return Mono.defer(() -> Mono.fromRunnable(() -> {
            Sinks.EmitResult emitResult = emitter.tryEmitNext(message);

            if (emitResult.isFailure()) {
                dlqSend(destination, new NullPointerException("destination can not match"));
            }
        }));
    }

    private void messageChannelSubscribe(Publisher<Message<?>> messagePublisher, String outgoing) {
        if (messagePublisher instanceof Mono<Message<?>>) {
            ((Mono<Message<?>>) messagePublisher)
                    .flatMap(message -> sendAndForget(outgoing, message))
                    .onErrorResume(throwable -> {
                        // catch exception and forget exception
                        return Mono.fromRunnable(() -> dlqSend(outgoing, throwable));
                    })
                    .subscribe();
        } else if (messagePublisher instanceof Flux<Message<?>>) {
            ((Flux<Message<?>>) messagePublisher)
                    .flatMap(message -> sendAndForget(outgoing, message))
                    .onErrorResume(throwable -> {
                        // catch exception and forget exception
                        return Mono.fromRunnable(() -> dlqSend(outgoing, throwable));
                    })
                    .subscribe();
        }
    }

    private void dlqSend(String destination, Throwable throwable) {
        Sinks.Many<Message<?>> dlq = consumerRegistration.get(DLQ_DEFAULT);
        if (dlq != null) {
            DLQMessageConsumer.DLQMessage dlqMessage = new DLQMessageConsumer.DLQMessage(destination, throwable);
            Message<DLQMessageConsumer.DLQMessage> msg = Message.MessageBuilder.withPayload(dlqMessage).build();

            dlq.emitNext(msg, Sinks.EmitFailureHandler.FAIL_FAST);
        }
    }

    private static void tryGetWriteLock(Lock lock) {
        // get lock
        try {
            if (!lock.tryLock(1, TimeUnit.SECONDS)) {
                throw new RuntimeException("can not get lock; please check lock is dead-locked");
            }
        } catch (InterruptedException e) {
            throw new RuntimeException("can not get lock; please check lock is dead-locked");
        }
    }

}
