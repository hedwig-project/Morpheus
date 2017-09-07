package com.hedwig.morpheus.domain.enums;

/**
 * Created by hugo. All rights reserved.
 */
public enum ReportingType {
    MODULE_REGISTRATION("MODULE_REGISTRATION"),
    MODULE_REMOVAL("MODULE_REMOVAL"),
    EMPTY_PARAMETERS("EMPTY_PARAMETERS"),
    MODULE_PROPERTIES("MODULE_PROPERTIES"),
    MORPHEUS_PROPERTIES("MORPHEUS_PROPERTIES");

    private final String description;

    ReportingType(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
