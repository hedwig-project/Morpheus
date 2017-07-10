package com.hedwig.morpheus.domain.repository;

import com.hedwig.morpheus.domain.model.implementation.Module;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

/**
 * Created by hugo. All rights reserved.
 */
@Repository
public interface ModuleRepository extends CrudRepository<Module, Long> {
    Module findById(Long id);
}
