package com.hedwig.morpheus.websocket;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.hedwig.morpheus.domain.dto.MessageDto;
import com.hedwig.morpheus.domain.dto.configuration.ConfigurationDto;
import com.hedwig.morpheus.domain.implementation.Message;
import com.hedwig.morpheus.domain.implementation.MessageQueue;
import com.hedwig.morpheus.util.listener.DisconnectionListener;
import com.hedwig.morpheus.util.tools.JSONUtilities;
import com.hedwig.morpheus.websocket.configurationHandlers.interfaces.IMessageHandler;
import io.socket.client.Ack;
import io.socket.client.IO;
import io.socket.client.Socket;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Scope;
import org.springframework.core.convert.ConversionService;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Created by hugo. All rights reserved.
 */
@Component
@Scope("singleton")
public class MorpheusWebSocket {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    private final AtomicBoolean connectedToCloud;
    private final Socket socketIO;
    private final List<DisconnectionListener> disconnectionListeners;
    private final IMessageHandler messageHandler;
    private final MessageQueue backupMessageQueue;
    private final ConversionService conversionService;

    private final String url;
    private final String protocol;
    private final String host;
    private final String port;
    private final String morpheusSerialNumber;


    @Autowired
    public MorpheusWebSocket(Environment environment,
                             IMessageHandler messageHandler,
                             @Qualifier("backupMessageQueue") MessageQueue backupMessageQueue,
                             ConversionService conversionService) throws URISyntaxException {
        this.conversionService = conversionService;

        protocol = environment.getProperty("cloud.protocol");
        host = environment.getProperty("cloud.host");
        port = environment.getProperty("cloud.port");
        morpheusSerialNumber = environment.getProperty("morpheus.configuration.serialNumber");

        assert null != protocol : "Protocol not found in configuration";
        assert null != host : "Host not found in configuration";
        assert null != port : "Port not found in configuration";
        assert null != morpheusSerialNumber : "Serial number not found in configuration";

        url = String.format("%s://%s:%s", protocol, host, port);
        logger.info("Cloud URL: " + url);

        this.messageHandler = messageHandler;

        IO.Options socketOptions = new IO.Options();
        socketOptions.reconnection = true;
        socketOptions.forceNew = true;
        socketOptions.multiplex = false;
        socketIO = IO.socket(url, socketOptions);

        connectedToCloud = new AtomicBoolean(false);
        disconnectionListeners = new ArrayList<>();

        addConnectionRelatedListenersToWebSocket();
        addMessageListeners();

        this.backupMessageQueue = backupMessageQueue;
    }

//    TODO : Make multithreaded tests here

    private void addMessageListeners() {
        socketIO.on("configurationMessage", args -> {
            try {
                logger.info("A new configuration message arrived");
                ConfigurationDto configurationDto = JSONUtilities.deserialize((String) args[0], ConfigurationDto.class);
                String report = messageHandler.inputConfiguration(configurationDto);
                sendConfirmationReport(report);
            } catch (IOException e) {
                logger.error("Unable to deserialize configuration message", e);
            }
        });

        socketIO.on("actionRequest", args -> {
            try {
                if (args.length == 0) return;
                logger.info("A new actionRequest message has arrived");
                List<MessageDto> messageDtoList =
                        JSONUtilities.deserialize((String) args[0], new TypeReference<List<MessageDto>>() {
                        });
                messageHandler.inputActionRequest(messageDtoList);
            } catch (IOException e) {
                logger.error("Unable to deserialize actionRequest message", e);
            }
        });

        socketIO.on("dataTransmission", (Object... args) -> {
            try {
                if (args.length == 0) return;
                logger.info("A new dataTransmission message has arrived");
                List<MessageDto> messageDtoList =
                        JSONUtilities.deserialize((String) args[0], new TypeReference<List<MessageDto>>() {
                        });
                messageHandler.inputDataTransmission(messageDtoList);
            } catch (IOException e) {
                logger.error("Unable to deserialize dataTransmission message", e);
            }
        });
    }

    private void sendConfirmationReport(String report) {
        socketIO.emit("confirmationReport", report);
        logger.info("Configuration Report sent to cloud");
    }

    public void connect() throws URISyntaxException {
        socketIO.connect();
    }

    public synchronized void addDisconnectionListener(DisconnectionListener disconnectionListener) {
        disconnectionListeners.add(disconnectionListener);
    }

    private void addConnectionRelatedListenersToWebSocket() {
        socketIO.on(Socket.EVENT_CONNECT, args -> {
            logger.info("Connection with cloud established successfully.");
            connectedToCloud.set(true);
        }).on(Socket.EVENT_DISCONNECT, args -> {
            logger.warn("Morpheus disconnected from the cloud. Reconnecting.");
            connectedToCloud.set(false);
        }).on(Socket.EVENT_CONNECT_ERROR, args -> {
            logger.error("Error when trying to connect to cloud. Trying again.");
            connectedToCloud.set(false);
        }).on(Socket.EVENT_ERROR, (args) -> {
            logger.error("ERROR HERE");
        });
    }

    public boolean isConnected() {
        return connectedToCloud.get();
    }

    public void sendMessage(Message message) {
        if (socketIO.connected()) {
            prepareMessage(message);
        } else {
            logger.error("Morpheus is disconnected. Trying again");
            backupMessageQueue.push(message);
        }
    }

    private void prepareMessage(Message message) {
        MessageDto messageDto = conversionService.convert(message, MessageDto.class);
        String eventType = getEventType(message);

        try {
            socketIO.emit(eventType, JSONUtilities.serialize(messageDto), new Ack() {
                @Override
                public void call(Object... args) {
                    if (null != args && args.length > 0) {
                        switch (args[0].toString().toLowerCase()) {
                            case "ok":
                                logger.info(String.format("Message %s sent to cloud", messageDto.getMessageId()));
                                break;
                            default:
                                logger.error(String.format("Failed to send message %s to cloud",
                                                           messageDto.getMessageId()));
                        }
                    }

                }
            });
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
    }

    private String getEventType(Message message) {
        String eventType;
        switch (message.getType()) {
            case CONFIRMATION:
                eventType = "confirmation";
                break;
            case CONFIGURATION:
                eventType = "configuration";
                break;
            case DATA_TRANSMISSION:
                eventType = "dataTransmission";
                break;
            default:
                throw new IllegalStateException(String.format("Message %s has invalid state", message.getId()));
        }

        return eventType;
    }
}
