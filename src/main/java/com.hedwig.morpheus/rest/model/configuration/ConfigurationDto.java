package com.hedwig.morpheus.rest.model.configuration;

import java.util.List;

/**
 * Created by hugo. All rights reserved.
 */
public class ConfigurationDto {

    private String configurationId;
    private Long timestamp;
    private MorpheusConfigurationDto morpheusConfiguration;
    private List<ModuleConfigurationDto> modulesConfiguration;

    public ConfigurationDto() {
    }

    public ConfigurationDto(String configurationId,
                            Long timestamp,
                            MorpheusConfigurationDto morpheusConfiguration,
                            List<ModuleConfigurationDto> modulesConfiguration) {
        this.configurationId = configurationId;
        this.timestamp = timestamp;
        this.morpheusConfiguration = morpheusConfiguration;
        this.modulesConfiguration = modulesConfiguration;
    }

    public String getConfigurationId() {
        return configurationId;
    }

    public void setConfigurationId(String configurationId) {
        this.configurationId = configurationId;
    }

    public Long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Long timestamp) {
        this.timestamp = timestamp;
    }

    public MorpheusConfigurationDto getMorpheusConfiguration() {
        return morpheusConfiguration;
    }

    public void setMorpheusConfiguration(MorpheusConfigurationDto morpheusConfiguration) {
        this.morpheusConfiguration = morpheusConfiguration;
    }

    public List<ModuleConfigurationDto> getModulesConfiguration() {
        return modulesConfiguration;
    }

    public void setModulesConfiguration(List<ModuleConfigurationDto> modulesConfiguration) {
        this.modulesConfiguration = modulesConfiguration;
    }
}
