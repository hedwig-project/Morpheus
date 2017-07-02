package com.hedwig.morpheus.service.interfaces;

/**
 * Created by hugo. All rights reserved.
 */
public interface ITopicManager {
    boolean subscribe(String topic);

    boolean unsubscribe(String topic);
}
