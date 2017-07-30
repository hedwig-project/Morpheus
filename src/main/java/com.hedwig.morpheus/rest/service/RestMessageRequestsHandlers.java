package com.hedwig.morpheus.rest.service;

import com.hedwig.morpheus.rest.model.MessageDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Created by hugo. All rights reserved.
 */

@Component
@Scope(value = "singleton")
public class RestMessageRequestsHandlers {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private RestMessageHandler restMessageHandler;

    public void inputRequestMessages(List<MessageDto> messages, String type) {
        if (null == messages) {
            return;
        }


        for (MessageDto messageDto : messages) {
            sendActionRequest(messageDto, type);
        }
    }

    private void sendActionRequest(MessageDto messageDto, String type) {
        if (null == messageDto) {
            logger.warn("Invalid request message");
        }

        messageDto.setMessageType(type);

        if (validateMessage(messageDto)) {
            restMessageHandler.inputMessage(messageDto);
        } else {
            logger.warn("Invalid message");
        }
    }

    private boolean validateMessage(MessageDto messageDto) {
        if (null == messageDto ||
            null == messageDto.getMessageType() ||
            null == messageDto.getPayload() ||
            null == messageDto.getTopic()) {
            return false;
        }

        switch (messageDto.getMessageType()) {
            case "action_request":
            case "data_transmission":
               break;
            default:
                return false;
        }

        return true;
    }
}
