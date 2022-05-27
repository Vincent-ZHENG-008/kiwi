package com.zhengwenhuan.kiwi.messaging;

/**
 * @author zhengwenhuan@gdmcmc.cn
 */
public interface MessageRegistration {

    void register(String destination, MessageSupplier supplier);

    void register(MessageConsumer consumer);

    void register(MessageFunction function);

}
