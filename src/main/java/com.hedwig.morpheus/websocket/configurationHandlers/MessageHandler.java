package com.hedwig.morpheus.websocket.configurationHandlers;

import com.hedwig.morpheus.domain.enums.MessageType;
import com.hedwig.morpheus.domain.enums.QualityOfService;
import com.hedwig.morpheus.domain.implementation.Message;
import com.hedwig.morpheus.domain.implementation.Module;
import com.hedwig.morpheus.domain.dto.MessageDto;
import com.hedwig.morpheus.domain.dto.ConfigurationDto;
import com.hedwig.morpheus.domain.dto.ModuleConfigurationDto;
import com.hedwig.morpheus.domain.dto.MorpheusConfigurationDto;
import com.hedwig.morpheus.domain.dto.RegistrationDto;
import com.hedwig.morpheus.service.interfaces.IMessageManager;
import com.hedwig.morpheus.service.interfaces.IModuleManager;
import com.hedwig.morpheus.websocket.configurationHandlers.interfaces.IMessageHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.core.convert.ConversionService;
import org.springframework.stereotype.Component;

import java.security.InvalidParameterException;
import java.util.List;

/**
 * Created by hugo. All rights reserved.
 */

@Component
@Scope("singleton")
public class MessageHandler implements IMessageHandler {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    private final ConversionService conversionService;

    private final IModuleManager moduleManager;
    private final IMessageManager messageManager;

    @Autowired
    public MessageHandler(IModuleManager moduleManager,
                          IMessageManager messageManager,
                          ConversionService conversionService) {
        this.conversionService = conversionService;
        this.moduleManager = moduleManager;
        this.messageManager = messageManager;
    }

    @Override
    public void inputConfiguration(ConfigurationDto configurationDto) {
        if (null == configurationDto) {
            logger.error("Configuration message did not follow the protocol",
                         new InvalidParameterException("Invalid configuration message"));
        }

        makeMorpheusConfiguration(configurationDto.getMorpheusConfiguration());
        makeModulesConfiguration(configurationDto.getModulesConfiguration());
    }

    @Override
    public void inputActionRequest(List<MessageDto> actionRequestMessages) {
        if (null == actionRequestMessages) {
            return;
        }

        for (MessageDto messageDto : actionRequestMessages) {
            processMessage(messageDto, MessageType.ACTION_REQUEST);
        }
    }

    @Override
    public void inputDataTransmission(List<MessageDto> dataTransmissionMessages) {
        if (null == dataTransmissionMessages) {
            return;
        }

        for (MessageDto messageDto : dataTransmissionMessages) {
            processMessage(messageDto, MessageType.DATA_TRANSMISSION);
        }
    }

    private void processMessage(MessageDto messageDto, MessageType type) {
        if (null == messageDto) {
            logger.warn("Invalid request message");
        }

//        TODO : Register uniqueID
        if(!validateMessage(messageDto)) {
            logger.error("Invalid message");
            return;
        }

        messageDto.setMessageType(type.toStringRepresentation());

        Message convertedMessage = conversionService.convert(messageDto, Message.class);
        if (null == convertedMessage) {
            logger.error("Could not process message with topic: " + messageDto.getTopic());
            return;
        }

        sendMessageToBroker(convertedMessage);
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

        if (null == module) {
            logger.error(String.format("Not possible to find module with topic %s", moduleTopic));
            return;
        }

        List<MessageDto> messages = moduleConfiguration.getMessages();

        if (messages != null && messages.size() > 0) {
            messages.stream()
                    .forEach(m -> assembleMessage(m,
                                                  moduleId,
                                                  moduleName,
                                                  module.getPublishToTopic()));
        }
    }

    private void assembleMessage(MessageDto message, Long moduleId, String moduleName, String topic) {
        message.setTopic(topic);
        message.setMessageType("configuration");

        Message convertedMessage = conversionService.convert(message, Message.class);
        if (null == convertedMessage) {
            logger.error("Could not process message with topic: " + message.getTopic());
            return;
        }

        sendMessageToBroker(convertedMessage);
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

    private void sendMessageToBroker(Message message) {
        if (null == message) {
            logger.warn("Message cannot be null");
        }

        messageManager.sendMessage(message);
    }

    private boolean validateMessage(MessageDto messageDto) {
        if (null == messageDto ||
            null == messageDto.getMessageType() ||
            null == messageDto.getPayload() ||
            null == messageDto.getTopic()) {
            return false;
        }

        switch (messageDto.getMessageType()) {
            case "action_request":
            case "data_transmission":
                break;
            default:
                return false;
        }

        return true;
    }
}
