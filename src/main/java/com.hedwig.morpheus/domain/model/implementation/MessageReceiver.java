package com.hedwig.morpheus.domain.model.implementation;

import com.hedwig.morpheus.domain.model.interfaces.IMessageReceiver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by hugo. All rights reserved.
 */
@Component
@Scope("singleton")
public class MessageReceiver implements IMessageReceiver {

    private static final int MAX_POOL_SIZE = 10;
    private final Logger logger = LoggerFactory.getLogger(MessageReceiver.class);

    private final MessageQueue incomeMessageQueue;

    private final ExecutorService threadPool = Executors.newFixedThreadPool(MAX_POOL_SIZE, new ThreadFactory() {
        private AtomicInteger atomicCounter = new AtomicInteger(1);

        @Override
        public Thread newThread(Runnable r) {
            Thread thread = new Thread(r);
            thread.setName(String.format("ReceiverThread-%d", atomicCounter.getAndIncrement()));
            thread.setDaemon(true);
            return thread;
        }
    });

    @Autowired
    public MessageReceiver(@Qualifier("incomeMessageQueue") MessageQueue incomeMessageQueue) {
        this.incomeMessageQueue = incomeMessageQueue;
    }

    @Override
    public void processIncomeMessage(Message message) {
        try {
            logger.info("Starting message processing");
            Thread.sleep(5000);
            logger.info("Message successfully parsed: " + message.getTopic());
        } catch (InterruptedException e) {
            logger.error("Error processing message", e);
        }
    }

    @Override
    public void processQueue() {
        threadPool.execute(() -> {
            while (true) {
                Message message = incomeMessageQueue.pop();
                threadPool.execute(() -> processIncomeMessage(message));
            }
        });
    }
}
