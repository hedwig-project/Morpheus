package com.hedwig.morpheus.domain.dto;

import java.io.Serializable;

/**
 * Created by hugo. All rights reserved.
 */
public class ControlParameterDto implements Serializable {

    private String parameter;
    private String value;

    public ControlParameterDto() {
    }

    public ControlParameterDto(String parameter, String value) {
        this.parameter = parameter;
        this.value = value;
    }

    public String getParameter() {
        return parameter;
    }

    public void setParameter(String parameter) {
        this.parameter = parameter;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }
}
