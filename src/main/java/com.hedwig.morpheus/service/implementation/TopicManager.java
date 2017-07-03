package com.hedwig.morpheus.service.implementation;

import com.hedwig.morpheus.domain.model.interfaces.IServer;
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
    public void subscribe(String topic) {
        if (subscribedTopics.contains(topic)) {
            logger.info(String.format("Topic %s is already in subscription list", topic));
            return;
        }

        Runnable successfullySubscribed = () -> {
            subscribedTopics.add(topic);
            logger.info(String.format("Successfully subscribed to topic %s", topic));
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

        subscribedTopics.remove(topic);
        return server.unsubscribe(topic);
    }

    @Override
    public boolean isSubscribed(String topic) {
        return subscribedTopics.contains(topic);
    }
}
