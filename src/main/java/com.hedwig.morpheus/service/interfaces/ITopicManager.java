package com.hedwig.morpheus.service.interfaces;

import com.hedwig.morpheus.domain.implementation.Result;

/**
 * Created by hugo. All rights reserved.
 */
public interface ITopicManager {
    Result subscribe(String topic);

    Result unsubscribe(String topic);

    boolean isSubscribed(String topic);
}
