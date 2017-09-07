package com.hedwig.morpheus.service.interfaces;

import com.hedwig.morpheus.domain.implementation.Module;
import com.hedwig.morpheus.domain.implementation.Result;

/**
 * Created by hugo. All rights reserved.
 */
public interface IModuleManager {
    Module getModuleByTopic(String topic);

    Result registerModule(Module module);

    Result removeModuleById(String id);

    Result removeModuleByTopic(String topic);

    boolean containsModuleByTopic(String topic);

    boolean containsModule(Module module);
}
