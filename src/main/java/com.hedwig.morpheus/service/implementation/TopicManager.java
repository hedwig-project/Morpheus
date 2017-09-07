package com.hedwig.morpheus.service.implementation;

import com.hedwig.morpheus.domain.interfaces.IServer;
import com.hedwig.morpheus.service.interfaces.ITopicManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.LinkedList;
import java.util.List;

/**
 * Created by hugo. All rights reserved.
 */
@Component
@Scope("singleton")
public class TopicManager implements ITopicManager {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final List<String> subscribedTopics;
    private final IServer server;

    @Autowired
    public TopicManager(IServer server) {
        this.subscribedTopics = new LinkedList<>();
        this.server = server;
    }

    @Override
    public void subscribe(String topic, String moduleName, Runnable successfullyRegistered) {
        if (subscribedTopics.contains(topic)) {
            logger.info(String.format("Topic %s is already in subscription list", topic));
            return;
        }

        Runnable successfullySubscribed = () -> {
            subscribedTopics.add(topic);

            if(successfullyRegistered != null) {
                successfullyRegistered.run();
            }

            logger.info(String.format("Successfully subscribed to topic %s", topic));
            logger.info(String.format("Module %s registered", moduleName));
        };

        Runnable failureInSubscription = () -> {
            logger.error(String.format("Failed to subscribe to topic %s", topic));
        };

        server.subscribe(topic, successfullySubscribed, failureInSubscription);
    }

    @Override
    public boolean unsubscribe(String topic) {
        if(!subscribedTopics.contains(topic))
            return false;

        if(server.unsubscribe(topic)) {
            return subscribedTopics.remove(topic);
        } else {
            logger.error(String.format("Failure when unsubscribing from topic %s", topic));
        }

        return false;
    }

    @Override
    public boolean isSubscribed(String topic) {
        return subscribedTopics.contains(topic);
    }
}
