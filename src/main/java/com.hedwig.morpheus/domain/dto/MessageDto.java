package com.hedwig.morpheus.domain.dto;

import java.io.Serializable;
import java.util.List;

/**
 * Created by hugo. All rights reserved.
 */
public class MessageDto implements Serializable {

    private String payload;

    private String topic;

    private String messageType;

    private List<ControlParameterDto> controlParameters;

    public MessageDto(String payload,
                      String topic,
                      String messageType,
                      List<ControlParameterDto> controlParameters) {
        this.payload = payload;
        this.topic = topic;
        this.messageType = messageType;
        this.controlParameters = controlParameters;
    }

    public MessageDto() {
    }

    public List<ControlParameterDto> getControlParameters() {
        return controlParameters;
    }

    public void setControlParameters(List<ControlParameterDto> controlParameters) {
        this.controlParameters = controlParameters;
    }

    public String getTopic() {
        return topic;
    }

    public void setTopic(String topic) {
        this.topic = topic;
    }

    public String getMessageType() {
        return messageType;
    }

    public void setMessageType(String messageType) {
        this.messageType = messageType;
    }

    public String getPayload() {
        return payload;
    }

    public void setPayload(String payload) {
        this.payload = payload;
    }
}
