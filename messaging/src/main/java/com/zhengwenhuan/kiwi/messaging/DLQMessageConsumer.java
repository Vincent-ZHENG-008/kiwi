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
                String incoming = message.getIncoming();
                Throwable throwable = message.getThrowable();

                logger.log(Level.SEVERE, "DLQ accept message, incoming: " + incoming + ", error: " + throwable.getLocalizedMessage());
            }
        });
    }

    public static final class DLQMessage {

        private String incoming;

        private Throwable throwable;

        public String getIncoming() {
            return incoming;
        }

        public void setIncoming(String incoming) {
            this.incoming = incoming;
        }

        public Throwable getThrowable() {
            return throwable;
        }

        public void setThrowable(Throwable throwable) {
            this.throwable = throwable;
        }

        @Override
        public String toString() {
            return "DLQMessage{" +
                    "incoming='" + incoming + '\'' +
                    ", throwable=" + throwable +
                    '}';
        }
    }
}
