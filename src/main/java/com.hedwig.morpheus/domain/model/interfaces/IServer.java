package com.hedwig.morpheus.domain.model.interfaces;

import org.eclipse.paho.client.mqttv3.MqttException;

/**
 * Created by hugo. All rights reserved.
 */
public interface IServer {

    String getConnectionUrl();

    void connect() throws MqttException;

    void shutdown();

    boolean subscribe(String topic);

    boolean unsubscribe(String topic);
}
