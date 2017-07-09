package com.hedwig.morpheus.converter;

import com.hedwig.morpheus.domain.model.implementation.Message;
import com.hedwig.morpheus.service.implementation.MqttMessageParser;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.springframework.core.convert.converter.Converter;

/**
 * Created by hugo. All rights reserved.
 */
public class MqttMessageToMessageConverter implements Converter<MqttMessage, Message> {
    @Override
    public Message convert(MqttMessage source) {
        Message message = MqttMessageParser.parse(source, "");

        return message;
    }
}
