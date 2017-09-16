package com.hedwig.morpheus.domain.dto;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by hugo. All rights reserved.
 */
public class MessageDto implements Serializable {

    private String messageId;

    private Map<String, String> payload;

    private String topic;

    private String messageType;

    private List<ControlParameterDto> controlParameters;

    public MessageDto(Map<String, String> payload,
                      String topic,
                      String messageType,
                      List<ControlParameterDto> controlParameters) {
        this.payload = new HashMap<>();
        this.payload.putAll(payload);
        this.topic = topic;
        this.messageType = messageType;
        this.controlParameters = controlParameters;
    }

    public MessageDto() {
        payload = new HashMap<>();
    }

    public String getMessageId() {
        return messageId;
    }

    public void setMessageId(String messageId) {
        this.messageId = messageId;
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

    public Map<String, String> getPayload() {
        return payload;
    }

    public void setPayload(Map<String, String> payload) {
        this.payload.putAll(payload);
    }
}
