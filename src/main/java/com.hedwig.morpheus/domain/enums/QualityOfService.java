package com.hedwig.morpheus.domain.enums;

/**
 * Created by hugo. All rights reserved.
 */
public enum QualityOfService {
    FIRST_LEVEL(1), SECOND_LEVEL(2), THIRD_LEVEL(3);

    private final int qosLevel;

    QualityOfService(int qosLevel) {
        this.qosLevel = qosLevel;
    }

    public static QualityOfService getQosFromInteger(Integer qos) {
        switch (qos) {
            case 3:
                return THIRD_LEVEL;
            case 2:
                return SECOND_LEVEL;
            default:
                return FIRST_LEVEL;
        }
    }

    public int getQosLevel() {
        return qosLevel;
    }
}