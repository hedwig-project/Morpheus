package com.hedwig.morpheus.business;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.hedwig.morpheus.domain.model.implementation.Message;
import com.hedwig.morpheus.util.json.JSONUtilities;
import com.hedwig.morpheus.websocket.MorpheusWebSocket;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Scope;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

/**
 * Created by hugo. All rights reserved.
 */

@Component
@Scope("singleton")
public class Cloud {
    private final String serialNumber;
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Value("${api.host}")
    private String apiHost;

    @Value("${api.port}")
    private String apiPort;

    @Value("${api.endpoint.configuration}")
    private String apiConfiguration;

    @Value("${api.endpoint.confirmation}")
    private String apiConfirmation;

    @Value("${api.endpoint.data_transmission}")
    private String apiDataTransmission;

    @Value("${morpheus.configuration.keepAlive}")
    private int period;


    @Autowired
    public Cloud(Environment environment) {
        serialNumber = environment.getProperty("morpheus.configuration.serialNumber");
    }

    // TODO : Actually send messages to cloud

    public void sendConfirmationMessage(Message message) {
        String logMessage =
                String.format(String.format("Confirmation message from topic %s sent to cloud", message.getId()));

        String urlString = String.format("http://%s:%s/%s", apiHost, apiPort, apiConfirmation);

        sendMessageToCloud(urlString, message.toString(), logMessage);
    }

    public void sendDataTransmissionMessage(Message message) {
        String logMessage =
                String.format(String.format("Data transmission message from topic %s sent to cloud", message.getId()));

        String urlString = String.format("http://%s:%s/%s", apiHost, apiPort, apiDataTransmission);

        sendMessageToCloud(urlString, message.toString(), logMessage);
    }

    public void sendConfigurationMessage(Message message) {
        String logMessage =
                String.format(String.format("Data request message from topic %s sent to cloud", message.getId()));

        String urlString = String.format("http://%s:%s/%s", apiHost, apiPort, apiConfiguration);
        String jsonString;

        try {
            jsonString = JSONUtilities.serialize(message);
        } catch (JsonProcessingException e) {
            logger.error("Unable to convert message to json", e);
            return;
        }

        sendMessageToCloud(urlString, jsonString, logMessage);
    }

//    private void sendMessageToCloud(String urlString, String message, String logMessage) {
//        try {
//            URL url = new URL(urlString);
//            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
//            connection.setDoOutput(true);
//            connection.setRequestMethod("POST");
//            DataOutputStream write = new DataOutputStream(connection.getOutputStream());
//            write.writeBytes(message);
//            write.flush();
//            write.close();
//
//            int responseCode = connection.getResponseCode();
//
//            switch (responseCode) {
//                case 200:
//                    logger.info(logMessage);
//                    break;
//                default:
//                    logger.error(String.format("Request to API returned with code %d", responseCode));
//            }
//
//        } catch (IOException e) {
//            logger.error("Could not open connection", e);
//        }
//    }

    private void sendMessageToCloud(String urlString, String message, String logMessage) {
        MorpheusWebSocket.sendConfirmationMessage(message);
    }
}
