package com.zhengwenhuan.kiwi.messaging;

import org.junit.jupiter.api.Test;
import reactor.core.publisher.Mono;

/**
 * @author zhengwenhuan@gdmcmc.cn
 */
class MessageDistributorTest {

    @Test
    public void register() {
        MessageDistributor messageDistributor = new MessageDistributor();
        messageDistributor.register("xx", new MessageConsumer(System.out::println));
        messageDistributor.register("yy", new MessageSupplier(() -> "zhangsan"));
        messageDistributor.register("yy", "xx", new MessageFunction(str -> Mono.just("lisi")));
    }

}