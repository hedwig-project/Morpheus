package com.hedwig.morpheus.domain.dto.configuration;

import java.util.List;

/**
 * Created by hugo. All rights reserved.
 */
public class MorpheusConfigurationDto {
    private List<RegistrationDto> register;

    public MorpheusConfigurationDto() {

    }

    public MorpheusConfigurationDto(List<RegistrationDto> register) {
        this.register = register;
    }

    public List<RegistrationDto> getRegister() {
        return register;
    }

    public void setRegister(List<RegistrationDto> register) {
        this.register = register;
    }
}
