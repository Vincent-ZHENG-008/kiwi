package com.zhengwenhuan.kiwi.function.binding;

import org.eclipse.collections.api.factory.Sets;

import java.util.Collection;
import java.util.Objects;
import java.util.Set;

/**
 * @author zhengwenhuan@gdmcmc.cn
 */
public class FunctionRegistration<T> {

    private T target;

    private final Set<String> names = Sets.mutable.empty();

    public FunctionRegistration(T target, Collection<String> names) {
        this.target = target;
        this.names.addAll(names);
    }

    public FunctionRegistration<T> withTarget(T target) {
        this.target = Objects.requireNonNull(target);
        return this;
    }

    public Set<String> getNames() {
        return Set.copyOf(names);
    }
}
