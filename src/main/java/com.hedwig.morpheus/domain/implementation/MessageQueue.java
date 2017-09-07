package com.hedwig.morpheus.domain.implementation;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.TimeUnit;

/**
 * Created by hugo. All rights reserved.
 */
@Component
public class MessageQueue {

    public static int TIMEOUT = 20;
    public static Logger logger = LoggerFactory.getLogger(MessageQueue.class);

    private final BlockingQueue<Message> messageQueue;

    public MessageQueue() {
        this.messageQueue = new PriorityBlockingQueue<>();
    }

    public boolean push(Message message) {
        try {
            return messageQueue.offer(message, TIMEOUT, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            logger.error("Error in queueing up message", e);
            return false;
        }
    }

    public Message pop() {
        try {
            return messageQueue.take();
        } catch (InterruptedException e) {
            logger.error("Error in popping message from queue", e);
            return null;
        }
    }
}
