package com.hedwig.morpheus.service.interfaces;

import com.hedwig.morpheus.domain.model.implementation.Module;

/**
 * Created by hugo. All rights reserved.
 */
public interface IModuleManager {
    boolean registerModule(Module module);

    boolean removeModuleById(String id);

    boolean containsModule(Module module);
}
