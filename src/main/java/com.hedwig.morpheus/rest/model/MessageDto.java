package com.hedwig.morpheus.rest.model;

import java.util.List;

/**
 * Created by hugo. All rights reserved.
 */
public class MessageDto {

    private String payload;

    private String topic;

    private String messageType;

    private List<ControlParameterDto> controlParameterDtoList;

    public MessageDto(String payload,
                      String topic,
                      String messageType,
                      List<ControlParameterDto> controlParameterDtoList) {
        this.payload = payload;
        this.topic = topic;
        this.messageType = messageType;
        this.controlParameterDtoList = controlParameterDtoList;
    }

    public MessageDto() {
    }

    public List<ControlParameterDto> getControlParameterDtoList() {
        return controlParameterDtoList;
    }

    public void setControlParameterDtoList(List<ControlParameterDto> controlParameterDtoList) {
        this.controlParameterDtoList = controlParameterDtoList;
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
