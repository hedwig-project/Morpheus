package com.hedwig.morpheus.util.listener;

import com.hedwig.morpheus.util.event.DisconnectionEvent;

/**
 * Created by hugo. All rights reserved.
 */
public interface DisconnectionListener {

    public void disconnectionReceived(DisconnectionEvent disconnectionEvent);
}
