package com.hedwig.morpheus.business;

import com.hedwig.morpheus.EntryPoint;
import com.hedwig.morpheus.domain.interfaces.IMessageReceiver;
import com.hedwig.morpheus.domain.interfaces.IServer;
import com.hedwig.morpheus.service.interfaces.IMessageManager;
import com.hedwig.morpheus.service.interfaces.IModuleManager;
import com.hedwig.morpheus.service.interfaces.ITopicManager;
import com.hedwig.morpheus.websocket.MorpheusWebSocket;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.net.URISyntaxException;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Created by hugo on 21/05/17. All rights reserved.
 */
@Component
@Scope("singleton")
public class Morpheus {

    private final IServer server;
    private final IMessageManager messageManager;
    private final IModuleManager moduleManager;
    private final ITopicManager topicManager;
    private final IMessageReceiver messageReceiver;
    private final MorpheusWebSocket morpheusWebSocket;
    private final AtomicBoolean reconnectionScheduled;
    private final Timer scheduler;

    private final Logger logger = LoggerFactory.getLogger(EntryPoint.class);

    @Value("${api.host}")
    private String apiHost;

    @Value("${api.port}")
    private String apiPort;

    @Value("${api.endpoint.confirmation}")
    private String apiConfirmation;

    @Value("${morpheus.configuration.keepAlive}")
    private int period;

    @Autowired
    public Morpheus(IMessageManager messageManager,
                    IModuleManager moduleManager,
                    ITopicManager topicManager,
                    IServer server,
                    IMessageReceiver messageReceiver,
                    MorpheusWebSocket morpheusWebSocket) {
        this.messageManager = messageManager;
        this.moduleManager = moduleManager;
        this.topicManager = topicManager;
        this.server = server;
        this.messageReceiver = messageReceiver;
        this.morpheusWebSocket = morpheusWebSocket;

        this.scheduler = new Timer();
        this.reconnectionScheduled = new AtomicBoolean(false);
    }

    public void start() {
        if (!connectToServer()) return;
        messageReceiver.processQueue();

        morpheusWebSocket.addDisconnectionListener(disconnectionEvent -> {
            logger.info(String.format(
                    "Morpheus is trying to reconnect to the cloud. If it fails, new try will be scheduled in %d ms",
                    period));
            rescheduleConnectionToCloud();
        });
        try {
            morpheusWebSocket.connect();
        } catch (URISyntaxException e) {
            logger.error("Invalid URL", e);
            shutdown();
        }
    }

    private synchronized void rescheduleConnectionToCloud() {
        if (reconnectionScheduled.get()) return;

        reconnectionScheduled.set(true);

        scheduler.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                if (morpheusWebSocket.isConnected()) {
                    this.cancel();
                    reconnectionScheduled.set(false);
                } else {
                    try {
                        morpheusWebSocket.connect();
                    } catch (URISyntaxException e) {
                        logger.error("Invalid URL", e);
                        this.cancel();
                        shutdown();
                    }
                }
            }
        }, 0, period);
    }

//    private void scheduleWebSocketConnection() {
//        scheduler.scheduleAtFixedRate(new TimerTask() {
//            @Override
//            public void run() {
//                if (connectionToCloudActive.get()) return;
//
//                try {
//                    morpheusWebSocket.connect();
//                    connectionToCloudActive.set(true);
//                } catch (Exception e) {
//                    logger.error("Unable to connect to cloud. New try will be scheduled.", e);
//                    connectionToCloudActive.set(false);
//                }
//            }
//        }, 0, period);
//    }


//    private void startKeepAlive() {
//        scheduler.scheduleAtFixedRate(new TimerTask() {
//            @Override
//            public void run() {
//                String urlString = String.format("http://%s:%s/%s", apiHost, apiPort, apiConfirmation);
//                URL url;
//                try {
//                    url = new URL(urlString);
//                    HttpURLConnection connection = (HttpURLConnection) url.openConnection();
//
//                    connection.setRequestMethod("POST");
//                    connection.setDoOutput(true);
//
//                    DataOutputStream write = new DataOutputStream(connection.getOutputStream());
//
//                    write.writeBytes("Keep Alive");
//                    write.flush();
//                    write.close();
//
//                    int responseCode = connection.getResponseCode();
//
//                    switch (responseCode) {
//                        case 200:
//                            logger.info("Keep alive message sent successfully");
//                            break;
//                        default:
//                            logger.error(String.format("Keep alive request returned with code %d", responseCode));
//                    }
//
//                } catch (MalformedURLException e) {
//                    logger.error("Malformed connection URL", e);
//                } catch (IOException e) {
//                    logger.error("Could not send keep alive message to host", e);
//                }
//            }
//        }, 0, period);
//    }

    public void shutdown() {
        logger.info("Shutting down Morpheus");
        server.shutdown();
        scheduler.cancel();
    }

    private boolean connectToServer() {
        try {
            server.connect();
            logger.info("Successfully connected to broker");
        } catch (MqttException e) {
            logger.error("Unable to connect to broker. Shutting down Morpheus", e);
            System.exit(0);
        }
        return true;
    }
}
