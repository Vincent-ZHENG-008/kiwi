package com.zhengwenhuan.kiwi.messaging;

import org.junit.jupiter.api.Test;
import reactor.core.publisher.Flux;

import java.util.function.Function;

/**
 * @author zhengwenhuan@gdmcmc.cn
 */
class MessageDistributorTest {

    @Test
    public void register() {
        Function<Flux<String>, Flux<String>> function = flux -> flux.map(str -> str + "**");


        MessageDistributor messageDistributor = new MessageDistributor();
        messageDistributor.register("xx", new MessageConsumer(System.out::println));
        messageDistributor.register("yy", new MessageSupplier(() -> Flux.just("zhangsan", "lisi")));
        messageDistributor.register("yy", "xx", new MessageFunction(flux -> function.apply((Flux<String>) flux)));
    }

}