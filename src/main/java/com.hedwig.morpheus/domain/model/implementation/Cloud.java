package com.hedwig.morpheus.domain.model.implementation;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

/**
 * Created by hugo. All rights reserved.
 */

@Component
@Scope("singleton")
public class Cloud {
    private String hostAddress;
    private int port;

    private final String serialNumber;

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    public Cloud(Environment environment) {
        serialNumber = environment.getProperty("morpheus.configuration.serialNumber");
    }

    public String getHostAddress() {
        return hostAddress;
    }

    public void setHostAddress(String hostAddress) {
        this.hostAddress = hostAddress;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    // TODO : Implement keepAliveMessage

    public boolean sendKeepAliveToCloud() {
        return true;
    }

    // TODO : Actually send messages to cloud

    public void sendConfirmationMessage(Message message) {
        logger.info(String.format("Confirmation message from topic %s sent to cloud", message.getId()));
    }

    public void sendDataTransmissionMessage(Message message) {
        logger.info(String.format("Data transmission message from topic %s sent to cloud", message.getId()));
    }

    public void sendDataRequestMessage(Message message) {
        logger.info(String.format("Data request message from topic %s sent to cloud", message.getId()));
    }
}
