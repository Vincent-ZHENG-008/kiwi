package com.zhengwenhuan.kiwi.function.binding;

import com.zhengwenhuan.kiwi.messaging.Message;
import org.reactivestreams.Publisher;

import java.util.concurrent.Flow;
import java.util.function.Function;

/**
 * @author zhengwenhuan@gdmcmc.cn
 */
public interface MessageHandler<T, R> extends Function<T, Publisher<R>> {
}
