package com.hedwig.morpheus.websocket;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.hedwig.morpheus.domain.dto.MessageDto;
import com.hedwig.morpheus.domain.dto.configuration.ConfigurationDto;
import com.hedwig.morpheus.domain.implementation.Message;
import com.hedwig.morpheus.domain.implementation.MessageQueue;
import com.hedwig.morpheus.util.listener.DisconnectionListener;
import com.hedwig.morpheus.util.tools.JSONUtilities;
import com.hedwig.morpheus.util.tools.MessageAgeVerifier;
import com.hedwig.morpheus.websocket.messageHandlers.interfaces.IMessageHandler;
import io.socket.client.IO;
import io.socket.client.Socket;
import io.socket.engineio.client.EngineIOException;
import org.json.JSONObject;
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
    private final MessageAgeVerifier messageAgeVerifier;


    @Autowired
    public MorpheusWebSocket(Environment environment,
                             IMessageHandler messageHandler,
                             @Qualifier("backupMessageQueue") MessageQueue backupMessageQueue,
                             ConversionService conversionService,
                             MessageAgeVerifier messageAgeVerifier) throws URISyntaxException {
        this.conversionService = conversionService;
        this.messageAgeVerifier = messageAgeVerifier;

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

    private void addMessageListeners() {
        socketIO.on("configuration", args -> {
            try {
                if (args.length < 2) return;
                logger.info("A new configuration message arrived");

                String payload = args[1].toString();

                ConfigurationDto configurationDto = JSONUtilities.deserialize(payload, ConfigurationDto.class);

                String report = messageHandler.inputConfiguration(configurationDto);
                sendConfirmationReport(report);

            } catch (IOException e) {
                logger.error("Unable to deserialize configuration message", e);
            }
        });

        socketIO.on("action", args -> {
            try {
                if (args.length < 2) return;
                logger.info("A new actionRequest message has arrived");

                String payload = "[" + args[1].toString() + "]";

                List<MessageDto> messageDtoList =
                        JSONUtilities.deserialize(payload, new TypeReference<List<MessageDto>>() {
                        });
                messageHandler.inputActionRequest(messageDtoList);
            } catch (IOException e) {
                logger.error("Unable to deserialize actionRequest message", e);
            }
        });

        socketIO.on("data", (Object... args) -> {
            try {
                if (args.length < 2) return;
                logger.info("A new dataTransmission message has arrived");

                String payload = args[1].toString();

                List<MessageDto> messageDtoList =
                        JSONUtilities.deserialize(payload, new TypeReference<List<MessageDto>>() {
                        });
                messageHandler.inputDataTransmission(messageDtoList);
            } catch (IOException e) {
                logger.error("Unable to deserialize dataTransmission message", e);
            }
        });
    }

    private void sendConfirmationReport(String report) {
        Object[] arg = {"data", morpheusSerialNumber, report};
        socketIO.emit("report", arg);
        logger.info("Configuration Report sent to cloud");
    }

    private void addConnectionRelatedListenersToWebSocket() {
        socketIO.on(Socket.EVENT_CONNECT, args -> {
            logger.info("Connection with cloud established successfully.");
            connectedToCloud.set(true);
        })
                .on(Socket.EVENT_DISCONNECT, args -> {
                    logger.warn("Morpheus disconnected from the cloud. Reconnecting.");
                    connectedToCloud.set(false);
                })
                .on(Socket.EVENT_CONNECT_ERROR, args -> {
                    logger.error("Error when trying to connect to cloud. Trying again.");
                    logger.error(((EngineIOException) args[0]).getMessage().toString());
                    logger.error(((EngineIOException) args[0]).getCause().toString());
                    connectedToCloud.set(false);
                })
                .on(Socket.EVENT_ERROR, (args) -> {
                    logger.error("ERROR HERE");
                })
                .on(Socket.EVENT_RECONNECT, (args) -> sendHelloEvent());
    }

    private void sendHelloEvent() {

        JSONObject morpheusHello = new JSONObject();
        morpheusHello.put("morpheusId", morpheusSerialNumber);
        morpheusHello.put("type", "morpheus");

        String[] arg = {morpheusSerialNumber, morpheusHello.toString()};

        socketIO.emit("hello", arg, args -> {
            if (null != args && args.length > 0) {
                switch (args[0].toString()
                               .toLowerCase()) {
                    case "ok":
                        logger.info(String.format("Cloud has received morpheus serial number"));
                        break;
                    default:
                        logger.error("Cloud has not received morpheus serial number");
                }
            }

        });
    }

    public void connect() throws URISyntaxException {
        socketIO.connect();
        sendHelloEvent();
    }

    public synchronized void addDisconnectionListener(DisconnectionListener disconnectionListener) {
        disconnectionListeners.add(disconnectionListener);
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

        if (messageAgeVerifier.isMessageTooOld(message)) {
            logger.warn(String.format("Message %s is too old and will be discarded", message.getId()));
            return;
        }

        try {
            Object[] arg = {morpheusSerialNumber, JSONUtilities.serialize(messageDto)};
            socketIO.emit(eventType, arg, args -> {
                if (null != args && args.length > 0) {
                    switch (args[0].toString()
                                   .toLowerCase()) {
                        case "ok":
                            logger.info(String.format("Message %s sent to cloud", messageDto.getMessageId()));
                            break;
                        default:
                            logger.error(String.format("Failed to send message %s to cloud",
                                                       messageDto.getMessageId()));
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
                eventType = "data";
                break;
            default:
                throw new IllegalStateException(String.format("Message %s has invalid state", message.getId()));
        }

        return eventType;
    }
}
