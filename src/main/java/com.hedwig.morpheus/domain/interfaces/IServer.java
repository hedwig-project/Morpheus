package com.hedwig.morpheus.domain.interfaces;

import com.hedwig.morpheus.domain.implementation.Result;
import org.eclipse.paho.client.mqttv3.MqttException;

/**
 * Created by hugo. All rights reserved.
 */
public interface IServer {

    String getConnectionUrl();

    void connect() throws MqttException;

    void shutdown();

    Result subscribe(String topic);

    Result unsubscribe(String topic);
}
