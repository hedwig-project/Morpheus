package com.hedwig.morpheus.business;

import com.hedwig.morpheus.domain.implementation.Message;
import com.hedwig.morpheus.domain.implementation.MessageQueue;
import com.hedwig.morpheus.domain.interfaces.IMessageReceiver;
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

    private final Cloud cloud;

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
    public MessageReceiver(@Qualifier("incomeMessageQueue") MessageQueue incomeMessageQueue, Cloud cloud) {
        this.incomeMessageQueue = incomeMessageQueue;
        this.cloud = cloud;
    }

    @Override
    public void processIncomeMessage(Message message) {
        switch (message.getType()) {
            case CONFIRMATION:
            case DATA_TRANSMISSION:
            case CONFIGURATION:
                cloud.sendMessageToCloud(message);
                break;
            default:
                throw new IllegalArgumentException("Invalid message type");
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
