package com.zhengwenhuan.kiwi.converter;

import java.util.function.Function;

/**
 * @author zhengwenhuan@gdmcmc.cn
 */
public interface Converter<T, R> extends Function<T, R> {

    default R convert(T source) {
        return apply(source);
    }

}
