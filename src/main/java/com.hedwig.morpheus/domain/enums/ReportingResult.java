package com.hedwig.morpheus.domain.enums;

/**
 * Created by hugo. All rights reserved.
 */
public enum ReportingResult {
    OKAY("Okay"),
    REGISTERED("Registered"),
    REMOVED("Removed"),
    FAILED("Failed"),
    DUPLICATE("Duplicate"),
    NON_EXISTING("Non existing"),
    UNAUTHORIZED("Unauthorized");

    private final String description;

    ReportingResult(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
