package com.zhengwenhuan.kiwi.messaging;

import org.junit.jupiter.api.Test;
import reactor.core.publisher.Flux;

/**
 * @author zhengwenhuan@gdmcmc.cn
 */
class MessageDistributorTest {

    @Test
    public void register() {

        MessageDistributor messageDistributor = new MessageDistributor();
        messageDistributor.register("xx", new MessageConsumer(System.out::println));
        messageDistributor.register("yy", "xx", new MessageFunction(flux -> flux.map(str -> str.payload() + "**")));
        messageDistributor.register("yy", new MessageSupplier(() -> Flux.just("zhangsan", "lisi")));
    }

}