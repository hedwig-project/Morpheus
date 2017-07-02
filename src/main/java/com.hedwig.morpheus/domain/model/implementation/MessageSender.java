package com.hedwig.morpheus.domain.model.implementation;

import com.hedwig.morpheus.domain.model.interfaces.IMessageSender;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

/**
 * Created by hugo. All rights reserved.
 */
@Component
public class MessageSender implements IMessageSender {
    private final Logger logger = LoggerFactory.getLogger(MessageReceiver.class);

    private final MessageQueue outputMessageQueue;

    @Autowired
    public MessageSender(@Qualifier("outputMessageQueue") MessageQueue outputMessageQueue) {
        this.outputMessageQueue = outputMessageQueue;
    }

    @Override
    public void send(Message message) {
        // TODO : Verify if topic is in subscription list
        outputMessageQueue.push(message);
        logger.info("Message pushed to the outputMessageQueue successfully");
    }
}
