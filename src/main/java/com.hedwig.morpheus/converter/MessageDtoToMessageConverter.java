package com.hedwig.morpheus.converter;

import com.hedwig.morpheus.domain.dto.ControlParameterDto;
import com.hedwig.morpheus.domain.dto.MessageDto;
import com.hedwig.morpheus.domain.enums.MessageType;
import com.hedwig.morpheus.domain.implementation.Message;
import org.springframework.core.convert.converter.Converter;

import java.util.List;

/**
 * Created by hugo. All rights reserved.
 */
public class MessageDtoToMessageConverter implements Converter<MessageDto, Message> {

    @Override
    public Message convert(MessageDto source) {
        MessageType type = getMessageTypeFromString(source.getMessageType());
        Message.MessageBody body = new Message.MessageBody(source.getPayload());

        Message message = new Message(source.getTopic(), type, body);

        List<ControlParameterDto> controlParameterDtoList = source.getControlParameters();

        if(controlParameterDtoList == null) return message;

        for (ControlParameterDto eachControlParameterDto : controlParameterDtoList) {
            Message.ControlParameter controlParameter =
                    new Message.ControlParameter(eachControlParameterDto.getParameter(),
                                                 eachControlParameterDto.getValue());
            message.addControlParameter(controlParameter);
        }

        return message;
    }

    private MessageType getMessageTypeFromString(String messageType) {
        switch (messageType) {
            case "configuration":
                return MessageType.CONFIGURATION;
            case "action_request":
                return MessageType.ACTION_REQUEST;
            case "data_transmission":
                return MessageType.DATA_TRANSMISSION;
            default:
                return null;
        }
    }
}
