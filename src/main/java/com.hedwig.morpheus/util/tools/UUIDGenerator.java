package com.hedwig.morpheus.util.tools;

import java.util.UUID;

/**
 * Created by hugo. All rights reserved.
 */
public class UUIDGenerator {

    private UUIDGenerator() {

    }

    public static UUID generateUUId() {
        UUID uniqueId = UUID.randomUUID();
        return uniqueId;
    }
}
