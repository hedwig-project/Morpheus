package com.hedwig.morpheus.service.implementation;

import com.hedwig.morpheus.domain.enums.MessageType;
import com.hedwig.morpheus.domain.implementation.Message;
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

        if (messageParts.size() <= 1) {
            logger.error("Invalid message", new IllegalArgumentException("Message does not follow protocol"));
            return null;
        }

        String stringType = messageParts.get(0);

        MessageType messageType = parseMessageType(stringType);

        if(messageType == null) {
            logger.error("Invalid message", new IllegalArgumentException("Message does not follow protocol"));
            return null;
        }

        List<Message.ControlParameter> controlParameters = new ArrayList<>();

        StringBuilder body = new StringBuilder();

        byte atCharCount = 0;

        for (String eachLine : messageParts) {
            if (eachLine.startsWith("$")) {
                controlParameters.add(parseControlParameter(eachLine));
            } else if(eachLine.startsWith("@")) {
                atCharCount++;
            } else if (!eachLine.startsWith("#")) {
                body.append(eachLine);
            }
        }

        if(atCharCount != 2) {
            logger.error("Invalid message", new IllegalArgumentException("Message does not follow protocol"));
            return null;
        }

        Message message = new Message(topic, messageType, new Message.MessageBody(body.toString()));

        controlParameters.stream().forEach(p -> message.addControlParameter(p));

        logger.info(String.format("Message %s successfully parsed", message.getId()));

        return message;
    }

    private static MessageType parseMessageType(String messageType) {
        String[] typeArray = messageType.split("#");

        if (typeArray.length < 2) {
            logger.error(String.format("Message type %s does not exist", messageType),
                         new IllegalArgumentException("Invalid message type"));
            return null;
        }

        String type = typeArray[1];

        switch (type) {
            case "configuration":
                return MessageType.CONFIGURATION;
            case "action_request":
                return MessageType.ACTION_REQUEST;
            case "confirmation":
                return MessageType.CONFIRMATION;
            case "data_transmission":
                return MessageType.DATA_TRANSMISSION;
            case "data_request":
                return MessageType.DATA_REQUEST;
        }

        logger.error(String.format("Message type %s does not exist", messageType),
                     new IllegalArgumentException("Invalid message type"));

        return null;
    }

    private static Message.ControlParameter parseControlParameter(String parameter) {
        String[] controlArray = parameter.split("$|:");

        if (controlArray.length < 2) {
            logger.error(String.format("Invalid control parameter %s", parameter),
                         new IllegalArgumentException("Control parameter does not exist"));
            return null;
        }

        return new Message.ControlParameter(controlArray[0], controlArray[1]);
    }
}
