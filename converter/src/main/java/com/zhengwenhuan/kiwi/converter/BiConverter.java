package com.zhengwenhuan.kiwi.converter;

import java.util.function.BiFunction;
import java.util.function.Function;

/**
 * @author zhengwenhuan@gdmcmc.cn
 */
public interface BiConverter<T1, T2, R> extends BiFunction<T1, T2, R> {

    default R convert(T1 source1, T2 source2) {
        return apply(source1, source2);
    }

}
