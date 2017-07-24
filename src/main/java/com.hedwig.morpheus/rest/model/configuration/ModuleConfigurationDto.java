package com.hedwig.morpheus.rest.model.configuration;

import com.hedwig.morpheus.rest.model.MessageDto;

import java.util.List;

/**
 * Created by hugo. All rights reserved.
 */
public class ModuleConfigurationDto {
    private Long moduleId;
    private String moduleName;
    private String moduleTopic;
    private Boolean unregister;
    private List<MessageDto> messages;

    public ModuleConfigurationDto() {
    }

    public ModuleConfigurationDto(Long moduleId,
                                  String moduleName,
                                  String moduleTopic,
                                  Boolean unregister,
                                  List<MessageDto> messages) {
        this.moduleId = moduleId;
        this.moduleName = moduleName;
        this.moduleTopic = moduleTopic;
        this.unregister = unregister;
        this.messages = messages;
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

    public Boolean getUnregister() {
        return unregister;
    }

    public void setUnregister(Boolean unregister) {
        this.unregister = unregister;
    }

    public List<MessageDto> getMessages() {
        return messages;
    }

    public void setMessages(List<MessageDto> messages) {
        this.messages = messages;
    }
}
