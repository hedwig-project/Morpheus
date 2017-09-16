package com.hedwig.morpheus.converter;

import com.hedwig.morpheus.domain.dto.ControlParameterDto;
import com.hedwig.morpheus.domain.dto.MessageDto;
import com.hedwig.morpheus.domain.implementation.Message;
import org.springframework.core.convert.converter.Converter;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by hugo. All rights reserved.
 */
public class MessageToMessageDtoConverter implements Converter<Message, MessageDto> {
    @Override
    public MessageDto convert(Message source) {
        MessageDto messageDto = new MessageDto();
        messageDto.setMessageId(source.getId());
        messageDto.setTopic(source.getTopic());
        messageDto.setMessageType(source.getType().toStringRepresentation());
        messageDto.setPayload(source.getBody().getPayload());

        List<Message.ControlParameter> controlParameterList = source.getControlParameters();
        List<ControlParameterDto> controlParameterDtoList = new ArrayList<>();

        if (controlParameterList == null) return messageDto;

        for (Message.ControlParameter eachControlParameter : controlParameterList) {
            ControlParameterDto controlParameterDto =
                    new ControlParameterDto(eachControlParameter.getParameter(), eachControlParameter.getValue());
            controlParameterDtoList.add(controlParameterDto);
        }

        messageDto.setControlParameters(controlParameterDtoList);
        return messageDto;
    }
}
