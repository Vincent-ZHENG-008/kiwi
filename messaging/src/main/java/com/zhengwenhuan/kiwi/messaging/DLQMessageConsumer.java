package com.zhengwenhuan.kiwi.messaging;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author zhengwenhuan@gdmcmc.cn
 */
public class DLQMessageConsumer extends MessageConsumer {

    private static final Logger logger = Logger.getLogger(DLQMessageConsumer.class.getName());

    public DLQMessageConsumer() {
        super(source -> {
            if (source instanceof DLQMessage) {
                DLQMessage message = (DLQMessage) source;
                String destination = message.getDestination();
                Throwable throwable = message.getThrowable();

                logger.log(Level.SEVERE, "DLQ accept message, destination: " + destination + ", error: " + throwable.getLocalizedMessage());
            }
        });
    }

    public static final class DLQMessage {

        private final String destination;

        private final Throwable throwable;

        public DLQMessage(String destination, Throwable throwable) {
            this.destination = destination;
            this.throwable = throwable;
        }

        public String getDestination() {
            return destination;
        }

        public Throwable getThrowable() {
            return throwable;
        }

        @Override
        public String toString() {
            return "DLQMessage{" +
                    "incoming='" + destination + '\'' +
                    ", throwable=" + throwable +
                    '}';
        }
    }
}
