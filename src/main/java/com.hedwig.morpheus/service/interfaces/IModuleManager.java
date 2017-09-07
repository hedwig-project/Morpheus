package com.hedwig.morpheus.service.interfaces;

import com.hedwig.morpheus.domain.implementation.Module;

/**
 * Created by hugo. All rights reserved.
 */
public interface IModuleManager {
    Module getModuleByTopic(String topic);

    void registerModule(Module module);

    boolean removeModuleById(String id);

    boolean removeModuleByTopic(String topic);

    boolean containsModuleByTopic(String topic);

    boolean containsModule(Module module);
}
