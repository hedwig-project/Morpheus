package com.hedwig.morpheus.websocket;

import com.fasterxml.jackson.core.type.TypeReference;
import com.hedwig.morpheus.domain.dto.ConfigurationDto;
import com.hedwig.morpheus.domain.dto.MessageDto;
import com.hedwig.morpheus.domain.implementation.Message;
import com.hedwig.morpheus.util.json.JSONUtilities;
import com.hedwig.morpheus.util.listener.DisconnectionListener;
import com.hedwig.morpheus.websocket.configurationHandlers.interfaces.IMessageHandler;
import io.socket.client.IO;
import io.socket.client.Socket;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
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

    static {

    }

    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    private final AtomicBoolean connectedToCloud;
    private final Socket socketIO;
    private final List<DisconnectionListener> disconnectionListeners;
    // TODO : These should come from the yaml configuration
    private final String url = "http://ec2-54-232-254-163.sa-east-1.compute.amazonaws.com:9090";
    private final String morpheusId = "morpheusId-201709";
    private final IMessageHandler messageHandler;

    @Autowired
    public MorpheusWebSocket(IMessageHandler messageHandler) throws URISyntaxException {
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
    }

//    TODO : Make multithreaded tests here

    private void addMessageListeners() {
        socketIO.on("configurationMessage", args -> {
            try {
                logger.info("A new configuration message arrived");
                ConfigurationDto configurationDto = JSONUtilities.deserialize((String) args[0], ConfigurationDto.class);
                messageHandler.inputConfiguration(configurationDto);
            } catch (IOException e) {
                logger.error("Unable to deserialize configuration message", e);
            }
        });

        socketIO.on("actionRequest", args -> {
            try {
                logger.info("A new actionRequest message has arrived");
                if (args.length == 0) return;
                List<MessageDto> messageDtoList =
                        JSONUtilities.deserialize((String) args[0], new TypeReference<List<MessageDto>>() {
                        });
                messageHandler.inputActionRequest(messageDtoList);
            } catch (IOException e) {
                logger.error("Unable to deserialize actionRequest message", e);
            }
        });

        socketIO.on("dataTransmission", args -> {
            try {
                logger.info("A new dataTransmission message has arrived");
                if (args.length == 0) return;
                List<MessageDto> messageDtoList =
                        JSONUtilities.deserialize((String) args[0], new TypeReference<List<MessageDto>>() {
                        });
                messageHandler.inputDataTransmission(messageDtoList);
            } catch (IOException e) {
                logger.error("Unable to deserialize dataTransmission message", e);
            }
        });
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
        });
    }

    public boolean isConnected() {
        return connectedToCloud.get();
    }

//    private void fireDisconnectionEvent(String cause) {
//        synchronized (disconnectionListeners) {
//            DisconnectionEvent disconnectionEvent = new DisconnectionEvent(this, cause);
//            Iterator listeners = disconnectionListeners.iterator();
//            while (listeners.hasNext()) {
//                ((DisconnectionListener) listeners.next()).disconnectionReceived(disconnectionEvent);
//            }
//        }
//    }

    public void sendConfirmationMessage(Message message) {
//        socketIO.send(message);

//        TODO : Serialize and get message type
        socketIO.emit("confirmation", message);
    }
}
