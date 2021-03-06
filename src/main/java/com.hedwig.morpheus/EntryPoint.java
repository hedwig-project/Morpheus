package com.hedwig.morpheus;

import com.hedwig.morpheus.business.Morpheus;
import com.hedwig.morpheus.domain.implementation.MessageQueue;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Created by hugo. All rights reserved.
 */

@SpringBootApplication
@Configuration
public class EntryPoint implements ApplicationRunner {

    @Override
    public void run(ApplicationArguments args) throws Exception {
    }

    public static void main(String[] args) {
        ApplicationContext context = SpringApplication.run(EntryPoint.class, args);
        Morpheus morpheus = context.getBean(Morpheus.class);
        morpheus.start();


        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            if(morpheus != null) {
                morpheus.shutdown();
            }
        }));

        while (true);
    }

    @Bean(name = "incomeMessageQueue")
    public MessageQueue incomeMessageQueue() {
        return new MessageQueue();
    }

    @Bean(name = "outputMessageQueue")
    public MessageQueue outputMessageQueue() {
        return new MessageQueue();
    }

    @Bean(name = "backupMessageQueue")
    public MessageQueue backupMessageQueue() {
        return new MessageQueue();
    }
}
