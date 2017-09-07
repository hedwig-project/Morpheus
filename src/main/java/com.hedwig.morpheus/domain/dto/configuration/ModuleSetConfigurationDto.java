package com.hedwig.morpheus.domain.dto.configuration;

/**
 * Created by hugo. All rights reserved.
 */
public class ModuleSetConfigurationDto {
    private String configurationName;
    private String configurationValue;

    public ModuleSetConfigurationDto() {
    }

    public ModuleSetConfigurationDto(String configurationName, String configurationValue) {
        this.configurationName = configurationName;
        this.configurationValue = configurationValue;
    }

    public String getConfigurationName() {
        return configurationName;
    }

    public void setConfigurationName(String configurationName) {
        this.configurationName = configurationName;
    }

    public String getConfigurationValue() {
        return configurationValue;
    }

    public void setConfigurationValue(String configurationValue) {
        this.configurationValue = configurationValue;
    }
}
