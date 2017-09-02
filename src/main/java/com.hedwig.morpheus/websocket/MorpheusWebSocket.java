package com.hedwig.morpheus.websocket;

import com.hedwig.morpheus.rest.model.MessageDto;
import com.hedwig.morpheus.rest.model.configuration.ConfigurationDto;
import com.hedwig.morpheus.rest.service.RestConfigurationHandler;
import com.hedwig.morpheus.util.json.JSONUtilities;
import com.hedwig.morpheus.util.listener.DisconnectionListener;
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

    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    private final AtomicBoolean connectedToCloud;
    private static Socket socketIO;
    private final List<DisconnectionListener> disconnectionListeners;
    // TODO : These should come from the yaml configuration
    static String url = "http://ec2-54-232-254-163.sa-east-1.compute.amazonaws.com:9090";

//    IO.Options socketOptions;
    int period = 10000;
    String morpheusId = "morpheusId-201709";

    @Autowired
    private RestConfigurationHandler configurationHandler;

    static {
        IO.Options socketOptions = new IO.Options();
        socketOptions.reconnection = true;
        socketOptions.forceNew = true;
        socketOptions.multiplex = false;

        try {
            socketIO = IO.socket(url, socketOptions);
        } catch (URISyntaxException e) {
            System.exit(0);
        }
    }

    public MorpheusWebSocket() throws URISyntaxException {
        disconnectionListeners = new ArrayList<>();
        connectedToCloud = new AtomicBoolean(false);

        addConnectionRelatedListenersToWebSocket();
        addMessageListeners();
    }

//    public MorpheusWebSocket() throws URISyntaxException {
//        disconnectionListeners = new ArrayList<>();
//        connectedToCloud = new AtomicBoolean(false);
//
//        IO.Options socketOptions = new IO.Options();
//        socketOptions.reconnection = true;
//        socketOptions.forceNew = true;
//        socketOptions.multiplex = false;
//
//        socketIO = IO.socket(url, socketOptions);
//        addConnectionRelatedListenersToWebSocket();
//        addMessageListeners();
//    }

    private void addMessageListeners() {
        socketIO.on("configurationMessage", args -> {
            try {
                logger.info("Configuration message arrived");
                ConfigurationDto configurationDto = JSONUtilities.deserialize((String) args[0], ConfigurationDto.class);
                configurationHandler.inputNewConfiguration(configurationDto);
            } catch (IOException e) {
                logger.error("Unable to deserialize configuration message", e);
            }
        }).on("dataTransmission", args -> {
            try {
                logger.info("Message received - DataTransmission");
                if(args.length == 0) return;
                MessageDto messageDto = JSONUtilities.deserialize((String) args[0], MessageDto.class);
                logger.info(messageDto.toString());
            } catch (IOException e) {
                logger.error("Unable to deserialize configuration message", e);
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
//            JSONObject obj = (JSONObject) args[0];
//            fireDisconnectionEvent(obj.toString());
            connectedToCloud.set(false);
        }).on(Socket.EVENT_CONNECT_ERROR, args -> {
            logger.error("Error when trying to connect to cloud. Trying again.");
            connectedToCloud.set(false);
//            String cause = ((Exception) args[0]).getCause().getMessage();
//            fireDisconnectionEvent(cause);
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

    public static void sendConfirmationMessage(String message) {
//        socketIO.send(message);
        socketIO.emit("confirmation", message);
    }
}
