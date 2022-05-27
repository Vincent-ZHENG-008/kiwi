package com.zhengwenhuan.kiwi.messaging.annotation;

import java.lang.annotation.*;

/**
 * @author zhengwenhuan@gdmcmc.cn
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD, ElementType.TYPE})
public @interface Incoming {

    String value();

}
