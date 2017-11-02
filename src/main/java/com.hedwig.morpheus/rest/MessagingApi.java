package com.hedwig.morpheus.rest;

import com.hedwig.morpheus.domain.dto.MessageDto;
import com.hedwig.morpheus.domain.dto.configuration.ConfigurationDto;
import com.hedwig.morpheus.websocket.messageHandlers.MessageHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Created by hugo. All rights reserved.
 */

@RestController
@RequestMapping(EndpointAddresses.MESSAGING_API)
public class MessagingApi {
    private final MessageHandler messageHandler;

    @Autowired
    public MessagingApi(MessageHandler messageHandler) {
        this.messageHandler = messageHandler;
    }

    @RequestMapping(path = EndpointAddresses.CONFIGURATION, method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<String> configurationMessage(@RequestBody ConfigurationDto configuration) {
        String report = messageHandler.inputConfiguration(configuration);

        return new ResponseEntity<>("configuration message received", HttpStatus.OK);
    }

    @RequestMapping(path = EndpointAddresses.ACTION_REQUEST, method = RequestMethod.POST)
    public ResponseEntity<String> actionRequestMessage(@RequestBody List<MessageDto> messages) {

        messageHandler.inputActionRequest(messages);
        return new ResponseEntity<>("action request message sent to module", HttpStatus.OK);
    }

    @RequestMapping(path = EndpointAddresses.DATA_TRANSMISSION, method = RequestMethod.POST)
    public ResponseEntity<String> dataTransmissionMessage(@RequestBody List<MessageDto> messages) {

        messageHandler.inputDataTransmission(messages);
        return new ResponseEntity<>("data transmission message sent to module", HttpStatus.OK);
    }
}
