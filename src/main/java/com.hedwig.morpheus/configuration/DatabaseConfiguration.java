package com.hedwig.morpheus.configuration;

import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.transaction.annotation.EnableTransactionManagement;

/**
 * Created by hugo. All rights reserved.
 */
@EnableJpaRepositories("com.hedwig.morpheus.repository")
@EnableTransactionManagement
public class DatabaseConfiguration {

}
