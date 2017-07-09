package com.hedwig.morpheus.service.implementation;

import com.hedwig.morpheus.domain.model.implementation.Message;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by hugo. All rights reserved.
 */
public class MqttMessageParser {

    private static final Logger logger = LoggerFactory.getLogger(MqttMessageParser.class);

    public static Message parse(MqttMessage mqttMessage) {
        return parse(mqttMessage, "");
    }

    public static Message parse(MqttMessage mqttMessage, String topic) {
        String payload = new String(mqttMessage.getPayload());

        List<String> messageParts = Arrays.asList(payload.split("\n"));

        String stringType = messageParts.get(0);

        Message.MessageType messageType = parseMessageType(stringType);

        List<Message.ControlParameter> controlParameters = new ArrayList<>();

        StringBuilder body = new StringBuilder();

        for (String eachLine : messageParts) {
            if (eachLine.startsWith("$")) {
                controlParameters.add(parseControlParameter(eachLine));
            } else if (!eachLine.startsWith("@") && !eachLine.startsWith("#")) {
                body.append(eachLine);
            }
        }

        Message message = new Message(topic, messageType, new Message.MessageBody(body.toString()));

        controlParameters.stream().forEach(p -> message.addControlParameter(p));

        logger.info(String.format("Message %d successfully parsed", message.getId()));

        return message;
    }

    private static Message.MessageType parseMessageType(String messageType) {
        String type = messageType.split("#")[1];
        switch (type) {
            case "configuration":
                return Message.MessageType.CONFIGURATION;
            case "action_request":
                return Message.MessageType.ACTION_REQUEST;
            case "confirmation":
                return Message.MessageType.CONFIRMATION;
            case "data_transmission":
                return Message.MessageType.DATA_TRANSMISSION;
            case "data_request":
                return Message.MessageType.DATA_REQUEST;
        }

        logger.error(String.format("Message type %s does not exist", messageType), new IllegalStateException());

        return null;
    }

    private static Message.ControlParameter parseControlParameter(String parameter) {
        String[] controlArray = parameter.split("$|:");

        if (controlArray.length < 2) {
            logger.error(String.format("Invalid control parameter %s", parameter), new IllegalStateException());
        }

        return new Message.ControlParameter(controlArray[0], controlArray[1]);
    }
}
