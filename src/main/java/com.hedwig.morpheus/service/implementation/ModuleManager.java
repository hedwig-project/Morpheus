package com.hedwig.morpheus.service.implementation;

import com.hedwig.morpheus.service.interfaces.IModuleManager;
import com.hedwig.morpheus.service.interfaces.ITopicManager;
import com.hedwig.morpheus.domain.model.implementation.Module;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Created by hugo. All rights reserved.
 */
@Component
@Scope("singleton")
public class ModuleManager implements IModuleManager {
    private final List modules;

    private final ITopicManager topicManager;

    @Autowired
    public ModuleManager(ITopicManager topicManager) {
        this.modules = new ArrayList<>();
        this.topicManager = topicManager;
    }

    @Override
    public boolean registerModule(Module module) {
        if (modules.contains(module)) return false;

        modules.add(module);
        topicManager.subscribe(module.getSubscribeToTopic());
        return true;
    }

    @Override
    public boolean removeModuleById(String id) {
        if (id == null) return false;

        Iterator<Module> iterator = modules.iterator();
        while (iterator.hasNext()) {
            Module module = iterator.next();
            if (id.equals(module.getId())) {
                topicManager.unsubscribe(module.getSubscribeToTopic());
                iterator.remove();
                return true;
            }
        }

        return false;
    }

    @Override
    public boolean containsModule(Module module) {
        return modules.stream().anyMatch(m -> m.equals(module));
    }
}