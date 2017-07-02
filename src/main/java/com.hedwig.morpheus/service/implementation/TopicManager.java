package com.hedwig.morpheus.service.implementation;

import com.hedwig.morpheus.domain.model.interfaces.IServer;
import com.hedwig.morpheus.service.interfaces.ITopicManager;
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

    private final List<String> subscribedTopics;
    private final IServer server;

    @Autowired
    public TopicManager(IServer server) {
        this.subscribedTopics = new LinkedList<>();
        this.server = server;
    }

    @Override
    public boolean subscribe(String topic) {
        if (subscribedTopics.contains(topic)) return false;

        subscribedTopics.add(topic);
        return server.subscribe(topic);
    }

    @Override
    public boolean unsubscribe(String topic) {
        if(!subscribedTopics.contains(topic))
            return false;

        subscribedTopics.remove(topic);
        return server.unsubscribe(topic);
    }
}
