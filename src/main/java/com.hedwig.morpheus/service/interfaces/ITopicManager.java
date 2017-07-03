package com.hedwig.morpheus.service.interfaces;

/**
 * Created by hugo. All rights reserved.
 */
public interface ITopicManager {
    void subscribe(String topic);

    boolean unsubscribe(String topic);

    boolean isSubscribed(String topic);
}
