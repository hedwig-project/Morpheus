package com.hedwig.morpheus.domain.model.interfaces;

import com.hedwig.morpheus.domain.model.implementation.Message;

/**
 * Created by hugo. All rights reserved.
 */
public interface IMessageSender {
    void send(Message message);
}
