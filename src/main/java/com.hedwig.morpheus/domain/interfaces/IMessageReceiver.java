package com.hedwig.morpheus.domain.interfaces;

import com.hedwig.morpheus.domain.implementation.Message;

/**
 * Created by hugo. All rights reserved.
 */
public interface IMessageReceiver {
    void processIncomeMessage(Message message);

    void processQueue();
}
