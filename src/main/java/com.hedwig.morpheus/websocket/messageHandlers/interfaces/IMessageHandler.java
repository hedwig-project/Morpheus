package com.hedwig.morpheus.websocket.messageHandlers.interfaces;

import com.hedwig.morpheus.domain.dto.MessageDto;
import com.hedwig.morpheus.domain.dto.configuration.ConfigurationDto;

import java.util.List;

/**
 * Created by hugo. All rights reserved.
 */
public interface IMessageHandler {

    String inputConfiguration(ConfigurationDto configurationDto);
    void inputActionRequest(List<MessageDto> actionRequestMessages);
    void inputDataTransmission(List<MessageDto> dataTransmissionMessages);

}
