package com.hedwig.morpheus.util.tools;

import com.hedwig.morpheus.domain.dto.ControlParameterDto;
import com.hedwig.morpheus.domain.dto.MessageDto;
import com.hedwig.morpheus.domain.dto.configuration.ConfigurationDto;
import com.hedwig.morpheus.domain.implementation.Message;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

/**
 * Created by hugo. All rights reserved.
 */
@Component
public class MessageAgeVerifier {

    private final boolean discardOldMessages;
    private final int discardAfter;

    public MessageAgeVerifier(Environment environment) {
        discardOldMessages =
                environment.getProperty("morpheus.configuration.messages.discardOldMessages", boolean.class, true);
        discardAfter = environment.getProperty("morpheus.configuration.messages.discardAfter", int.class, 30);
    }


    public boolean isMessageTooOld(ConfigurationDto configurationDto) {
        if(!discardOldMessages) {
            return false;
        }

        Long timeStamp = configurationDto.getTimestamp();
        Long now = System.currentTimeMillis() / 1000;

        return now >= timeStamp + discardAfter;
    }

    public boolean isMessageTooOld(Message message) {
       if(!discardOldMessages) {
           return false;
       }

       return isMessageStaled(message);
    }

    public boolean isMessageTooOld(MessageDto message) {
        if(!discardOldMessages) {
            return false;
        }

        return isMessageStaled(message);
    }

    private boolean isMessageStaled(Message message) {
        Long timeStamp = getMessageTimeStamp(message);
        Long now = System.currentTimeMillis() / 1000;

        return now >= timeStamp + discardAfter;
    }

    private boolean isMessageStaled(MessageDto message) {
        Long timeStamp = getMessageTimeStamp(message);
        Long now = System.currentTimeMillis() / 1000;

        return now >= timeStamp + discardAfter;
    }

    private Long getMessageTimeStamp(Message message) {
        List<Message.ControlParameter> controlParameters = message.getControlParameters();
        Optional<Message.ControlParameter> controlParameter = controlParameters.stream()
                                                                               .filter(cp -> cp.getParameter()
                                                                                               .equals("ts"))
                                                                               .findFirst();

        String timeStampString =
                controlParameter.orElseThrow(() -> new IllegalArgumentException("No timestamp found in message"))
                                .getValue();

        return Long.parseLong(timeStampString);
    }

    private Long getMessageTimeStamp(MessageDto message) {
        List<ControlParameterDto> controlParameters = message.getControlParameters();
        Optional<ControlParameterDto> controlParameter = controlParameters.stream()
                                                                               .filter(cp -> cp.getParameter()
                                                                                               .equals("ts"))
                                                                               .findFirst();

        String timeStampString =
                controlParameter.orElseThrow(() -> new IllegalArgumentException("No timestamp found in message"))
                                .getValue();

        return Long.parseLong(timeStampString);
    }
}
