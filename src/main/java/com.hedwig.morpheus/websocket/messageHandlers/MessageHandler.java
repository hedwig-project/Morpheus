package com.hedwig.morpheus.websocket.messageHandlers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.hedwig.morpheus.domain.dto.MessageDto;
import com.hedwig.morpheus.domain.dto.configuration.ConfigurationDto;
import com.hedwig.morpheus.domain.dto.configuration.ModuleConfigurationDto;
import com.hedwig.morpheus.domain.dto.configuration.MorpheusConfigurationDto;
import com.hedwig.morpheus.domain.dto.configuration.RegistrationDto;
import com.hedwig.morpheus.domain.enums.MessageType;
import com.hedwig.morpheus.domain.enums.QualityOfService;
import com.hedwig.morpheus.domain.enums.ReportingResult;
import com.hedwig.morpheus.domain.enums.ReportingType;
import com.hedwig.morpheus.domain.implementation.Message;
import com.hedwig.morpheus.domain.implementation.Module;
import com.hedwig.morpheus.domain.implementation.Report;
import com.hedwig.morpheus.domain.implementation.Result;
import com.hedwig.morpheus.service.implementation.BackupMessageService;
import com.hedwig.morpheus.service.implementation.ConfigurationReporter;
import com.hedwig.morpheus.service.interfaces.IMessageManager;
import com.hedwig.morpheus.service.interfaces.IModuleManager;
import com.hedwig.morpheus.util.tools.JSONUtilities;
import com.hedwig.morpheus.util.tools.UUIDGenerator;
import com.hedwig.morpheus.websocket.messageHandlers.interfaces.IMessageHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.core.convert.ConversionService;
import org.springframework.stereotype.Component;

import java.security.InvalidParameterException;
import java.util.List;
import java.util.UUID;

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
    private final ConfigurationReporter configurationReporter;
    private final String MORPHEUS_ID = "mopheus-configuration";

    @Autowired
    public MessageHandler(IModuleManager moduleManager,
                          IMessageManager messageManager,
                          ConversionService conversionService,
                          ConfigurationReporter configurationReporter) {
        this.conversionService = conversionService;
        this.moduleManager = moduleManager;
        this.messageManager = messageManager;
        this.configurationReporter = configurationReporter;
    }

    @Override
    public String inputConfiguration(ConfigurationDto configurationDto) {
        UUID uuid = UUIDGenerator.generateUUId();

        if (null == configurationDto) {
            Report report = new Report().reportType(ReportingType.EMPTY_PARAMETERS)
                                        .reportResult(ReportingResult.FAILED)
                                        .reportDescription("The configuration message did not follow the protocol");

            configurationReporter.addReport(uuid, report);

            logger.error("Configuration message did not follow the protocol",
                         new InvalidParameterException("Invalid configuration message"));

        } else {
            makeMorpheusConfiguration(configurationDto.getMorpheusConfiguration(), uuid);
            makeModulesConfiguration(configurationDto.getModulesConfiguration(), uuid);
        }

        return configurationReporter.generateReportForConfiguration(uuid);
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
        if (!validateMessage(messageDto)) {
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

    private void makeModulesConfiguration(List<ModuleConfigurationDto> modulesConfiguration, UUID uuid) {
        if (null == modulesConfiguration) {
            return;
        }

        for (ModuleConfigurationDto eachModuleConfiguration : modulesConfiguration) {
            if (moduleManager.containsModuleByTopic(eachModuleConfiguration.getModuleTopic())) {
                configureEachModule(eachModuleConfiguration, uuid);
            } else {
                logger.info(String.format("Module %s not yet registered", eachModuleConfiguration.getModuleName()));
            }
        }
    }

    private void configureEachModule(ModuleConfigurationDto moduleConfiguration, UUID uuid) {
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
            Result result = moduleManager.removeModuleByTopic(moduleTopic);
            if (result.isSuccess()) {
                logger.info(String.format("Module %s removed successfully", moduleName));
                Report report = new Report().reportIdentification(moduleId.toString())
                                            .reportType(ReportingType.MODULE_REMOVAL)
                                            .reportResult(ReportingResult.REMOVED)
                                            .reportDescription("Module removed successfully");
                configurationReporter.addReport(uuid, report);

            } else {
                logger.info(String.format("Not possible to remove module %s", moduleName));
                Report report = new Report().reportIdentification(moduleId.toString())
                                            .reportType(ReportingType.MODULE_REMOVAL)
                                            .reportResult(ReportingResult.FAILED)
                                            .reportDescription(result.getDescription());
                configurationReporter.addReport(uuid, report);
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
            messages.stream().forEach(m -> assembleMessage(m, moduleId, moduleName, module.getPublishToTopic()));
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

    private void makeMorpheusConfiguration(MorpheusConfigurationDto morpheusConfiguration, UUID uuid) {
        if (null == morpheusConfiguration) {
            return;
        }

        logger.info("Configuring Morpheus");

        makeModuleRegistrations(morpheusConfiguration.getRegister(), uuid);
        makeOtherMorpheusConfigurations(morpheusConfiguration, uuid);
    }

    private void makeOtherMorpheusConfigurations(MorpheusConfigurationDto morpheusConfiguration, UUID uuid) {
        if(morpheusConfiguration.isRequestSendingPersistedMessages()) {
            List<MessageDto> messageDtos = BackupMessageService.readSerializedMessagesInFile();
            Report report = null;
            try {
                report = new Report().reportIdentification(MORPHEUS_ID)
                                            .reportType(ReportingType.MORPHEUS_REQUEST)
                                            .reportResult(ReportingResult.OKAY)
                                            .reportDescription(JSONUtilities.serialize(messageDtos));
            } catch (JsonProcessingException e) {
                logger.info("Error in processing serialized messages");
            }

            configurationReporter.addReport(uuid, report);
            logger.info("Persisted messages were required");
        }
    }

    private void makeModuleRegistrations(List<RegistrationDto> register, UUID uuid) {
        if (null == register || register.size() == 0) {
            logger.info("No modules to be registered");
            return;
        }

        register.stream().forEach(registration -> registerModule(registration, uuid));
    }

    private void registerModule(RegistrationDto registrationDto, UUID uuid) {
        if (null == registrationDto) {
            logger.info("Empty registration information");
            return;
        }

        Module module = new Module(registrationDto.getModuleId(),
                                   registrationDto.getModuleName(),
                                   registrationDto.getModuleTopic());

        module.setQualityOfService(QualityOfService.getQosFromInteger(registrationDto.getQos()));
        module.configureReceiveMessagesAtMostEvery(registrationDto.getReceiveMessagesAtMostEvery());

        Result result = moduleManager.registerModule(module);

        if (result.isSuccess()) {
            Report report = new Report().reportIdentification(module.getId().toString())
                                        .reportType(ReportingType.MODULE_REGISTRATION)
                                        .reportResult(ReportingResult.REGISTERED)
                                        .reportDescription("Module registered successfully");
            configurationReporter.addReport(uuid, report);
        } else {
            Report report = new Report().reportIdentification(module.getId().toString())
                                        .reportType(ReportingType.MODULE_REGISTRATION)
                                        .reportResult(ReportingResult.FAILED)
                                        .reportDescription(result.getDescription());
            configurationReporter.addReport(uuid, report);
        }
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
