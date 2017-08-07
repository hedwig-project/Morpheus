package com.hedwig.morpheus.business;

import com.hedwig.morpheus.EntryPoint;
import com.hedwig.morpheus.domain.model.interfaces.IMessageReceiver;
import com.hedwig.morpheus.domain.model.interfaces.IServer;
import com.hedwig.morpheus.service.interfaces.IMessageManager;
import com.hedwig.morpheus.service.interfaces.IModuleManager;
import com.hedwig.morpheus.service.interfaces.ITopicManager;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Timer;
import java.util.TimerTask;

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

    private final Logger logger = LoggerFactory.getLogger(EntryPoint.class);

    private final Timer scheduler;

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
                    IMessageReceiver messageReceiver) {
        this.messageManager = messageManager;
        this.moduleManager = moduleManager;
        this.topicManager = topicManager;
        this.server = server;
        this.messageReceiver = messageReceiver;

        scheduler = new Timer();
    }

    public void start() {
        if (!connectToServer()) return;
        messageReceiver.processQueue();

        startKeepAlive();
    }

    private void startKeepAlive() {
        scheduler.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                String urlString = String.format("http://%s:%s/%s", apiHost, apiPort, apiConfirmation);
                URL url;
                try {
                    url = new URL(urlString);
                    HttpURLConnection connection = (HttpURLConnection) url.openConnection();

                    connection.setRequestMethod("POST");
                    connection.setDoOutput(true);

                    DataOutputStream write = new DataOutputStream(connection.getOutputStream());

                    write.writeBytes("Keep Alive");
                    write.flush();
                    write.close();

                    int responseCode = connection.getResponseCode();

                    switch (responseCode) {
                        case 200:
                            logger.info("Keep alive message sent successfully");
                            break;
                        default:
                            logger.error(String.format("Keep alive request returned with code %d", responseCode));
                    }

                } catch (MalformedURLException e) {
                    logger.error("Malformed connection URL", e);
                } catch (IOException e) {
                    logger.error("Could not send keep alive message to host", e);
                }


            }
        }, 0, period);
    }

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
            logger.error("Can't start the server", e);
            System.exit(0);
        }
        return true;
    }
}
