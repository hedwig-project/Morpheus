package com.hedwig.morpheus.business;

import com.hedwig.morpheus.domain.implementation.Message;
import com.hedwig.morpheus.websocket.MorpheusWebSocket;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Scope;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

/**
 * Created by hugo. All rights reserved.
 */

@Component
@Scope("singleton")
public class Cloud {
    private final String serialNumber;
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final MorpheusWebSocket morpheusWebSocket;

    @Value("${api.host}")
    private String apiHost;

    @Value("${api.port}")
    private String apiPort;

    @Value("${api.endpoint.configuration}")
    private String apiConfiguration;

    @Value("${api.endpoint.confirmation}")
    private String apiConfirmation;

    @Value("${api.endpoint.data_transmission}")
    private String apiDataTransmission;

    @Value("${morpheus.configuration.keepAlive}")
    private int period;


    @Autowired
    public Cloud(Environment environment, MorpheusWebSocket morpheusWebSocket) {
        serialNumber = environment.getProperty("morpheus.configuration.serialNumber");
        this.morpheusWebSocket = morpheusWebSocket;
    }

    public void sendMessageToCloud(Message message) {
        morpheusWebSocket.sendMessage(message);
    }
}
