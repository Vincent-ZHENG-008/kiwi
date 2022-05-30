package com.zhengwenhuan.kiwi.messaging;

import org.eclipse.collections.impl.factory.Maps;
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

    private final Map<String, Sinks.Many<Message<Object>>> consumerRegistration = Maps.mutable.empty();
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
                Sinks.Many<Message<Object>> emitter = consumerRegistration.get(DLQ_DEFAULT);
                if (emitter == null) {
                    emitter = Sinks.many().multicast().onBackpressureBuffer(20);
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

            messageChannelSubscribe(outgoing, supplier.get());
        }
    }

    @Override
    public void register(String incoming, MessageConsumer consumer) {
        try (TerminateExecute ignored = new TerminateExecute(wLock::unlock)) {
            // get lock
            tryGetWriteLock(wLock);

            Sinks.Many<Message<Object>> emitter = consumerRegistration.get(incoming);
            if (emitter == null) {
                emitter = Sinks.many().multicast().onBackpressureBuffer(20);
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

            Sinks.Many<Message<Object>> emitter = consumerRegistration.get(incoming);
            if (emitter == null) {
                emitter = Sinks.many().multicast().onBackpressureBuffer(20);
                consumerRegistration.put(incoming, emitter);
            }

            messageChannelSubscribe(outgoing, function.apply(emitter));
        }
    }

    @Override
    public Mono<Void> sendAndForget(String destination, Message<Object> message) {
        Sinks.Many<Message<Object>> emitter = consumerRegistration.get(destination);
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

    private void messageChannelSubscribe(String outgoing, Flux<Message<Object>> messagePublisher) {
        messagePublisher.flatMap(message -> sendAndForget(outgoing, message))
                .onErrorResume(throwable -> Mono.fromRunnable(() -> dlqSend(outgoing, throwable)))
                .subscribe();
    }

    @SuppressWarnings("unchecked")
    private void dlqSend(String destination, Throwable throwable) {
        Sinks.Many<Message<Object>> dlq = consumerRegistration.get(DLQ_DEFAULT);
        if (dlq != null) {
            DLQMessageConsumer.DLQMessage dlqMessage = new DLQMessageConsumer.DLQMessage(destination, throwable);
            Message<?> msg = Message.MessageBuilder.withPayload(dlqMessage).build();

            dlq.emitNext((Message<Object>) msg, Sinks.EmitFailureHandler.FAIL_FAST);
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
