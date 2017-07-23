package com.hedwig.morpheus.rest.service;

import com.hedwig.morpheus.domain.model.enums.QualityOfService;
import com.hedwig.morpheus.domain.model.implementation.Module;
import com.hedwig.morpheus.rest.model.configuration.ConfigurationDto;
import com.hedwig.morpheus.rest.model.configuration.ModuleConfigurationDto;
import com.hedwig.morpheus.rest.model.configuration.MorpheusConfigurationDto;
import com.hedwig.morpheus.rest.model.configuration.RegistrationDto;
import com.hedwig.morpheus.service.interfaces.IModuleManager;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.security.InvalidParameterException;
import java.util.List;

/**
 * Created by hugo. All rights reserved.
 */

@Component
@Scope(value = "singleton")
public class RestConfigurationHandler {

    private final IModuleManager moduleManager;
    private final org.slf4j.Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    public RestConfigurationHandler(IModuleManager moduleManager) {
        this.moduleManager = moduleManager;
    }

    public void inputNewConfiguration(ConfigurationDto configuration) {

        if (null == configuration) {
            logger.error("Configuration message did not follow the protocol",
                         new InvalidParameterException("Invalid configuration message"));
        }

        makeMorpheusConfiguration(configuration.getMorpheusConfiguration());
        makeModulesConfiguration(configuration.getModulesConfiguration());
    }

    private void makeModulesConfiguration(List<ModuleConfigurationDto> modulesConfiguration) {
        if(null == modulesConfiguration) {
            return;
        }

        for(ModuleConfigurationDto eachModuleConfiguration : modulesConfiguration) {
            configureEachModule(eachModuleConfiguration);
        }
    }

    private void configureEachModule(ModuleConfigurationDto moduleConfiguration) {
        if(null == moduleConfiguration) {
            return;
        }

        Long moduleId = moduleConfiguration.getModuleId();
        String moduleName = moduleConfiguration.getModuleName();
        String moduleTopic = moduleConfiguration.getModuleTopic();

        if(null == moduleId || null == moduleName || null == moduleTopic) {
            logger.warn("Module information is not complete");
            return;
        }

        Boolean unregister = moduleConfiguration.getUnregister();
        if(unregister != null && unregister) {
            boolean removed = moduleManager.removeModuleByTopic(moduleTopic);
            if(removed) {
                logger.info(String.format("Module %s removed successfully", moduleName));
            } else {
                logger.info(String.format("Not possible to remove module %s", moduleName));
            }
        }
    }

    private void makeMorpheusConfiguration(MorpheusConfigurationDto morpheusConfiguration) {
        if (null == morpheusConfiguration) {
            return;
        }

        logger.info("Configuring Morpheus");

        makeModuleRegistrations(morpheusConfiguration.getRegister());
    }

    private void makeModuleRegistrations(List<RegistrationDto> register) {
        if (null == register || register.size() == 0) {
            logger.info("No modules to be registered");
        }

        register.stream().forEach(this::registerModule);
    }

    private void registerModule(RegistrationDto registrationDto) {
        if (null == registrationDto) {
            logger.info("Empty registration information");
            return;
        }

        Module module = new Module(registrationDto.getModuleId(),
                                   registrationDto.getModuleName(),
                                   registrationDto.getModuleTopic());

        module.setQualityOfService(QualityOfService.getQosFromInteger(registrationDto.getQos()));
        module.configureReceiveMessagesAtMostEvery(registrationDto.getReceiveMessagesAtMostEvery());


        boolean success = moduleManager.registerModule(module);
        if(success) {
            logger.info(String.format("Successfully registered module %s", module.getName()));
        } else {
            logger.info(String.format("Module %s already registered", module.getName()));
        }
    }
}
