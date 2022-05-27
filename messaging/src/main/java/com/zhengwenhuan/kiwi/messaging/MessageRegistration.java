package com.zhengwenhuan.kiwi.messaging;

/**
 * @author zhengwenhuan@gdmcmc.cn
 */
public interface MessageRegistration {

    void register(String outgoing, MessageSupplier supplier);

    void register(String incoming, MessageConsumer consumer);

    void register(String incoming, String outgoing, MessageFunction function);

}
