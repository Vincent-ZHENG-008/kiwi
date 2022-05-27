package com.zhengwenhuan.kiwi.messaging;

import java.util.function.Consumer;

/**
 * @author zhengwenhuan@gdmcmc.cn
 */
public interface MessageHandler extends Consumer<Message<?>> {
}
