package com.hedwig.morpheus.service.implementation;

import com.hedwig.morpheus.domain.implementation.Result;
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
    private final ConfigurationReporter configurationReporter;

    @Autowired
    public TopicManager(IServer server, ConfigurationReporter configurationReporter) {
        this.subscribedTopics = new LinkedList<>();
        this.server = server;
        this.configurationReporter = configurationReporter;
    }

    @Override
    public Result subscribe(String topic) {
        Result result = new Result();

        if (subscribedTopics.contains(topic)) {
            logger.info(String.format("Topic %s is already in subscription list", topic));
            result.setSuccess(false);
            result.setDescription(String.format("Topic %s is already in subscription list", topic));
            return result;
        }

        Result subscription = server.subscribe(topic);
        if (subscription.isSuccess()) {
            subscribedTopics.add(topic);
            logger.info(String.format("Successfully subscribed to topic %s", topic));
            result.setSuccess(true);
            result.setDescription(String.format("Successfully subscribed to topic %s", topic));
            return result;
        }

        logger.error(String.format("Failed to subscribe to topic %s", topic));
        result.setSuccess(false);
        result.setDescription(subscription.getDescription());
        return result;
    }

    @Override
    public Result unsubscribe(String topic) {
        Result result = new Result();

        if (!subscribedTopics.contains(topic)) {
            result.setSuccess(false);
            result.setDescription("Morpheus isn't subscribed to topic");
            return result;
        }

        Result unsubscribe = server.unsubscribe(topic);
        if (unsubscribe.isSuccess()) {
            subscribedTopics.remove(topic);
            logger.info(String.format("Successfully unsubscribed from topic %s", topic));
            result.setSuccess(true);
            result.setDescription(String.format("Successfully unsubscribed from topic %s", topic));
            return result;
        }

        logger.error(String.format("Failed to unsubscribe from topic %s", topic));
        result.setSuccess(false);
        result.setDescription(unsubscribe.getDescription());
        return result;
    }

    @Override
    public boolean isSubscribed(String topic) {
        return subscribedTopics.contains(topic);
    }
}
