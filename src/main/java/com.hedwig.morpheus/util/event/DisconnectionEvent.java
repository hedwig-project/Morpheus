package com.hedwig.morpheus.util.event;

import java.util.EventObject;

/**
 * Created by hugo. All rights reserved.
 */
public class DisconnectionEvent extends EventObject {

    private String cause;

    public DisconnectionEvent(Object source, String cause) {
        super(source);
        this.cause = cause;
    }

    public String getCause() {
        return cause;
    }
}
