package com.hedwig.morpheus.error;

/**
 * Created by hugo. All rights reserved.
 */
public class StaledMessageException extends Exception {

    public StaledMessageException(String message) {
        super(message);
    }
}
