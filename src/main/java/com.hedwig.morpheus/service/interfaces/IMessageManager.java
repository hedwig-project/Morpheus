package com.hedwig.morpheus.service.interfaces;

import com.hedwig.morpheus.domain.model.implementation.Message;
import org.eclipse.paho.client.mqttv3.MqttMessage;

/**
 * Created by hugo. All rights reserved.
 */
public interface IMessageManager {
    void processIncomeMessage(String topic, MqttMessage mqttMessage);
    void sendMessage(Message message);
}
