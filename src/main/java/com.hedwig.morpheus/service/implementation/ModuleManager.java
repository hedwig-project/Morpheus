package com.hedwig.morpheus.service.implementation;

import com.hedwig.morpheus.domain.model.implementation.Module;
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

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    public ModuleManager(ITopicManager topicManager, ModuleRepository moduleRepository) {
        this.modules = new ArrayList<>();
        this.topicManager = topicManager;
        this.moduleRepository = moduleRepository;
    }

    @Override
    public Module getModuleByTopic(String topic) {
        Optional<Module> module = modules.stream().filter(m -> m.getTopic().equals(topic)).findFirst();

        return module.orElse(null);
    }

    @Override
    public void registerModule(Module module) {
        if (modules.contains(module)) {
            logger.info(String.format("Module %s already registered", module.getName()));
            return;
        }

        modules.add(module);
        topicManager.subscribe(module.getSubscribeToTopic(), module.getName(), () -> moduleRepository.save(module));
    }

    @Override
    public boolean removeModuleById(String id) {
        if (id == null) return false;

        Iterator<Module> iterator = modules.iterator();
        while (iterator.hasNext()) {
            Module module = iterator.next();
            if (id.equals(module.getId())) {
                topicManager.unsubscribe(module.getSubscribeToTopic());
                moduleRepository.delete(module.getId());
                iterator.remove();
                return true;
            }
        }

        return false;
    }

    @Override
    public boolean removeModuleByTopic(String topic) {
        if (topic == null) return false;

        Iterator<Module> iterator = modules.iterator();
        while (iterator.hasNext()) {
            Module module = iterator.next();
            if (topic.equals(module.getTopic())) {
                if (topicManager.unsubscribe(module.getSubscribeToTopic())) {
                    moduleRepository.delete(module.getId());
                    iterator.remove();
                    return true;
                }
            }
        }

        return false;
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