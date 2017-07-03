package com.hedwig.morpheus.business;

import com.hedwig.morpheus.configuration.EntryPoint;
import com.hedwig.morpheus.domain.model.implementation.Module;
import com.hedwig.morpheus.domain.model.interfaces.IMessageReceiver;
import com.hedwig.morpheus.domain.model.interfaces.IServer;
import com.hedwig.morpheus.service.interfaces.IMessageManager;
import com.hedwig.morpheus.service.interfaces.IModuleManager;
import com.hedwig.morpheus.service.interfaces.ITopicManager;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

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
    }

    public void start() {
        if (!connectToServer()) return;
        messageReceiver.processQueue();

        Module kitchen = new Module("1", "kitchen", "hw/kitchen1");
        moduleManager.registerModule(kitchen);

        Module room = new Module("1", "room", "hw/room");
        moduleManager.registerModule(room);

        Module livingRoom = new Module("1", "livingRoom", "hw/livingRoom1");
        moduleManager.registerModule(livingRoom);
    }

    public void shutdown() {
        logger.info("Shutting down Morpheus");
        server.shutdown();
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
