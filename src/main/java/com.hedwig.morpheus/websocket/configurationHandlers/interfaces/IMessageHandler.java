package com.hedwig.morpheus.websocket.configurationHandlers.interfaces;

import com.hedwig.morpheus.domain.dto.MessageDto;
import com.hedwig.morpheus.domain.dto.ConfigurationDto;

import java.util.List;

/**
 * Created by hugo. All rights reserved.
 */
public interface IMessageHandler {

    void inputConfiguration(ConfigurationDto configurationDto);
    void inputActionRequest(List<MessageDto> actionRequestMessages);
    void inputDataTransmission(List<MessageDto> dataTransmissionMessages);

}
