package com.hedwig.morpheus.domain.model.interfaces;

import com.hedwig.morpheus.domain.model.implementation.Message;

/**
 * Created by hugo. All rights reserved.
 */
public interface IMessageReceiver {
    void processIncomeMessage(Message message);

    void processQueue();
}
