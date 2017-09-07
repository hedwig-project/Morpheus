package com.hedwig.morpheus.service.implementation;

import com.hedwig.morpheus.domain.implementation.Message;
import com.hedwig.morpheus.domain.interfaces.IMessageSender;
import com.hedwig.morpheus.service.interfaces.IMessageManager;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

/**
 * Created by hugo. All rights reserved.
 */
@Component
@Scope("singleton")
public class MessageManager implements IMessageManager {

    // TODO : Parse messages from module
    // TODO : Make message queue

    private final IMessageSender messageSender;


    @Autowired
    public MessageManager(IMessageSender messageSender) {
        this.messageSender = messageSender;
    }

    @Override
    public void processIncomeMessage(String topic, MqttMessage mqttMessage) {
        System.out.println(new String(mqttMessage.getPayload()));
    }

    @Override
    public void sendMessage(Message message) {
        messageSender.send(message);
    }
}