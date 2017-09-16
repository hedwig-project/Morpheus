package com.hedwig.morpheus.service.implementation;

import com.hedwig.morpheus.domain.implementation.Module;
import com.hedwig.morpheus.domain.implementation.Result;
import com.hedwig.morpheus.repository.ModuleRepository;
import com.hedwig.morpheus.service.interfaces.IModuleManager;
import com.hedwig.morpheus.service.interfaces.ITopicManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;

/**
 * Created by hugo. All rights reserved.
 */
@Component
@Scope("singleton")
public class ModuleManager implements IModuleManager {
    private final List<Module> modules;

    private final ITopicManager topicManager;
    private final ModuleRepository moduleRepository;
    private final ConfigurationReporter configurationReporter;
    private final ConfigurationPersister configurationPersister;

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    public ModuleManager(ITopicManager topicManager,
                         ModuleRepository moduleRepository,
                         ConfigurationReporter configurationReporter,
                         ConfigurationPersister configurationPersister) {
        this.modules = new ArrayList<>();
        this.topicManager = topicManager;
        this.moduleRepository = moduleRepository;
        this.configurationReporter = configurationReporter;
        this.configurationPersister = configurationPersister;
    }

    @Override
    public Module getModuleByTopic(String topic) {
        Optional<Module> module = modules.stream().filter(m -> m.getTopic().equals(topic)).findFirst();

        return module.orElse(null);
    }

    @Override
    public Result registerModule(Module module) {
        Result result = new Result();

        if (modules.contains(module)) {
            logger.info(String.format("Module %s already registered", module.getName()));
            result.setSuccess(false);
            result.setDescription(String.format("Module %s already registered", module.getName()));
            return result;
        }

        Result subscribedToTopic = topicManager.subscribe(module.getSubscribeToTopic());

        if (subscribedToTopic.isSuccess()) {
            logger.info(String.format("Module %s registered successfully", module.getName()));
            modules.add(module);
            result.setSuccess(true);
            result.setDescription(String.format("Module %s registered successfully", module.getName()));
            updateConfigurationPersister(module);
            return result;
        }

        logger.error(String.format("Module %s failed to be registered", module.getName()));
        result.setDescription(subscribedToTopic.getDescription());
        return result;
    }

    private void updateConfigurationPersister(Module module) {
        configurationPersister.updateModulesList(module);
    }

    @Override
    public Result removeModuleById(String id) {
        return removeModule(id, true);
    }

    @Override
    public Result removeModuleByTopic(String topic) {
        return removeModule(topic, false);
    }

    private Result removeModule(String key, boolean removeById) {
        Result result = new Result();

        if (null == key) {
            result.setSuccess(false);
            result.setDescription("No information about this module was provided");
            return result;
        }

        Iterator<Module> iterator = modules.iterator();
        while (iterator.hasNext()) {
            Module module = iterator.next();

            boolean found = removeById ? key.equals(module.getId()) : key.equals(module.getTopic());

            if (found) {
                Result unsubscribed = topicManager.unsubscribe(module.getSubscribeToTopic());
                if (unsubscribed.isSuccess()) {
                    moduleRepository.delete(module.getId());
                    logger.info(String.format("Module %s removed successfully", module.getName()));
                    iterator.remove();
                    result.setSuccess(true);
                    result.setDescription(String.format("Module %s removed successfully", module.getName()));
                    updateConfigurationPersister(module);
                    return result;
                } else {
                    logger.error(String.format("Module %s failed to be removed", module.getName()));
                    result.setSuccess(false);
                    result.setDescription(unsubscribed.getDescription());
                    return result;
                }
            }
        }

        logger.info(String.format("Module %s not found", key));
        result.setSuccess(false);
        result.setDescription("Module not found");
        return result;
    }

    @Override
    public boolean containsModuleByTopic(String topic) {
        return modules.stream().anyMatch(m -> m.getTopic().equals(topic));
    }

    @Override
    public boolean containsModule(Module module) {
        return modules.stream().anyMatch(m -> m.equals(module));
    }
}