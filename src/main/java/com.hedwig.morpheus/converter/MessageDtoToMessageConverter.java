package com.hedwig.morpheus.converter;

import com.hedwig.morpheus.domain.model.implementation.Message;
import com.hedwig.morpheus.rest.model.ControlParameterDto;
import com.hedwig.morpheus.rest.model.MessageDto;
import org.springframework.core.convert.converter.Converter;

import java.util.List;

/**
 * Created by hugo. All rights reserved.
 */
public class MessageDtoToMessageConverter implements Converter<MessageDto, Message> {

    @Override
    public Message convert(MessageDto source) {
        Message.MessageType type = getMessageTypeFromString(source.getMessageType());
        Message.MessageBody body = new Message.MessageBody(source.getPayload());

        Message message = new Message(source.getTopic(), type, body);

        List<ControlParameterDto> controlParameterDtoList = source.getControlParameterDtoList();

        if(controlParameterDtoList == null) return message;

        for (ControlParameterDto eachControlParameterDto : controlParameterDtoList) {
            Message.ControlParameter controlParameter =
                    new Message.ControlParameter(eachControlParameterDto.getParameter(),
                                                 eachControlParameterDto.getValue());
            message.addControlParameter(controlParameter);
        }

        return message;
    }

    private Message.MessageType getMessageTypeFromString(String messageType) {
        switch (messageType) {
            case "configuration":
                return Message.MessageType.CONFIGURATION;
            case "action_request":
                return Message.MessageType.ACTION_REQUEST;
            case "data_transmission":
                return Message.MessageType.DATA_TRANSMISSION;
            default:
                return null;
        }
    }
}
