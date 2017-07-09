package com.hedwig.morpheus.rest.service;

import com.hedwig.morpheus.domain.model.implementation.Message;
import com.hedwig.morpheus.rest.model.MessageDto;
import com.hedwig.morpheus.service.interfaces.IMessageManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.core.convert.ConversionService;
import org.springframework.stereotype.Component;

/**
 * Created by hugo. All rights reserved.
 */

@Component
@Scope(value = "singleton")
public class RestMessageHandler {

    private final ConversionService conversionService;

    private final IMessageManager messageManager;

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    public RestMessageHandler(ConversionService conversionService, IMessageManager messageManager) {
        this.conversionService = conversionService;
        this.messageManager = messageManager;
    }

    public void inputMessage(MessageDto messageDto) {
        Message convertedMessage = conversionService.convert(messageDto, Message.class);
        if (null == convertedMessage) {
            logger.error("Could not process message with topic: " + messageDto.getTopic());
            return;
        }

        messageManager.sendMessage(convertedMessage);
    }
}
