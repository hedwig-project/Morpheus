package com.hedwig.morpheus.domain.enums;

/**
 * Created by hugo. All rights reserved.
 */
public enum MessageType {
    ACTION_REQUEST("action_request"),
    CONFIGURATION("configuration"),
    CONFIRMATION("confirmation"),
    DATA_TRANSMISSION("data_transmission"),
    DATA_REQUEST("data_request");

    private final String stringRepresentation;

    private MessageType(String stringRepresentation) {
        this.stringRepresentation = stringRepresentation;
    }

    public String toStringRepresentation() {
        return stringRepresentation;
    }
}
