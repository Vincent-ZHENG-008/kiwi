package com.zhengwenhuan.kiwi.messaging;

/**
 * @author zhengwenhuan@gdmcmc.cn
 */
public final class TerminateExecute implements AutoCloseable {

    private final Runnable onTerminate;

    public TerminateExecute(Runnable onTerminate) {
        this.onTerminate = onTerminate;
    }

    @Override
    public void close() {
        onTerminate.run();
    }
}
