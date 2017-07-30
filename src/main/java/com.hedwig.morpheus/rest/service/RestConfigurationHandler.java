package com.hedwig.morpheus.rest.service;

import com.hedwig.morpheus.domain.model.enums.QualityOfService;
import com.hedwig.morpheus.domain.model.implementation.Module;
import com.hedwig.morpheus.rest.model.MessageDto;
import com.hedwig.morpheus.rest.model.configuration.ConfigurationDto;
import com.hedwig.morpheus.rest.model.configuration.ModuleConfigurationDto;
import com.hedwig.morpheus.rest.model.configuration.MorpheusConfigurationDto;
import com.hedwig.morpheus.rest.model.configuration.RegistrationDto;
import com.hedwig.morpheus.service.interfaces.IModuleManager;
import org.slf4j.Logger;
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
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    RestMessageHandler messageHandler;

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
        if (null == modulesConfiguration) {
            return;
        }

        for (ModuleConfigurationDto eachModuleConfiguration : modulesConfiguration) {
            if (moduleManager.containsModuleByTopic(eachModuleConfiguration.getModuleTopic())) {
                configureEachModule(eachModuleConfiguration);
            } else {
                logger.info(String.format("Module %s not yet registered", eachModuleConfiguration.getModuleName()));
            }
        }
    }

    private void configureEachModule(ModuleConfigurationDto moduleConfiguration) {
        if (null == moduleConfiguration) {
            return;
        }

        Long moduleId = moduleConfiguration.getModuleId();
        String moduleName = moduleConfiguration.getModuleName();
        String moduleTopic = moduleConfiguration.getModuleTopic();

        if (null == moduleId || null == moduleName || null == moduleTopic) {
            logger.warn("Module information is not complete");
            return;
        }

        Boolean unregister = moduleConfiguration.getUnregister();
        if (unregister != null && unregister) {
            boolean removed = moduleManager.removeModuleByTopic(moduleTopic);
            if (removed) {
                logger.info(String.format("Module %s removed successfully", moduleName));
            } else {
                logger.info(String.format("Not possible to remove module %s", moduleName));
            }

            return;
        }

        Module module = moduleManager.getModuleByTopic(moduleTopic);

        if(null == module) {
            logger.error(String.format("Not possible to find module with topic %s", moduleTopic));
            return;
        }

        List<MessageDto> messages = moduleConfiguration.getMessages();

        if (messages != null && messages.size() > 0) {
            messages.stream().forEach(m -> sendConfigurationMessages(m, moduleId, moduleName, module.getPublishToTopic()));
        }
    }

    private void sendConfigurationMessages(MessageDto message, Long moduleId, String moduleName, String topic) {
        message.setTopic(topic);
        message.setMessageType("configuration");
        messageHandler.inputMessage(message);
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


        moduleManager.registerModule(module);
    }
}
