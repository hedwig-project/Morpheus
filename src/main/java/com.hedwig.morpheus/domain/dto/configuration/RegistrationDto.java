package com.hedwig.morpheus.domain.dto.configuration;

/**
 * Created by hugo. All rights reserved.
 */
public class RegistrationDto {

    private Long moduleId;
    private String moduleName;
    private String moduleTopic;
    private String receiveMessagesAtMostEvery;
    private Integer qos;

    public RegistrationDto() {
    }

    public RegistrationDto(Long moduleId,
                           String moduleName,
                           String moduleTopic,
                           String receiveMessagesAtMostEvery,
                           Integer qos) {

        this.moduleId = moduleId;
        this.moduleName = moduleName;
        this.moduleTopic = moduleTopic;
        this.receiveMessagesAtMostEvery = receiveMessagesAtMostEvery;
        this.qos = qos;
    }

    public Long getModuleId() {
        return moduleId;
    }

    public void setModuleId(Long moduleId) {
        this.moduleId = moduleId;
    }

    public String getModuleName() {
        return moduleName;
    }

    public void setModuleName(String moduleName) {
        this.moduleName = moduleName;
    }

    public String getModuleTopic() {
        return moduleTopic;
    }

    public void setModuleTopic(String moduleTopic) {
        this.moduleTopic = moduleTopic;
    }

    public String getReceiveMessagesAtMostEvery() {
        return receiveMessagesAtMostEvery;
    }

    public void setReceiveMessagesAtMostEvery(String receiveMessagesAtMostEvery) {
        this.receiveMessagesAtMostEvery = receiveMessagesAtMostEvery;
    }

    public Integer getQos() {
        return qos;
    }

    public void setQos(Integer qos) {
        this.qos = qos;
    }
}
