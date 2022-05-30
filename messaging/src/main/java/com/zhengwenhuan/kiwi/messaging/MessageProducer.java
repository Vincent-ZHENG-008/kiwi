package com.zhengwenhuan.kiwi.messaging;

import reactor.core.publisher.Mono;

/**
 * @author zhengwenhuan@gdmcmc.cn
 */
public interface MessageProducer {

    Mono<Void> sendAndForget(String destination, Message<Object> message);

}
