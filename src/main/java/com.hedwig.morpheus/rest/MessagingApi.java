package com.hedwig.morpheus.rest;

import com.hedwig.morpheus.rest.model.MessageDto;
import com.hedwig.morpheus.rest.service.RestMessageHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

/**
 * Created by hugo. All rights reserved.
 */

@RestController
@RequestMapping(EndpointAddresses.MESSAGING_API)
public class MessagingApi {

    @Autowired
    RestMessageHandler messageHandler;

    @RequestMapping(path = EndpointAddresses.CONFIGURATION, method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<String> configurationMessage(@RequestBody MessageDto message) {

        message.setMessageType("configuration");
        messageHandler.inputMessage(message);

        return new ResponseEntity<>("configuration message received", HttpStatus.OK);
    }

    @RequestMapping(path = EndpointAddresses.ACTION_REQUEST, method = RequestMethod.POST)
    public ResponseEntity<String> actionRequestMessage(@RequestBody MessageDto message) {

        message.setMessageType("action_request");
        messageHandler.inputMessage(message);

        return new ResponseEntity<>("action request message received", HttpStatus.OK);
    }

    @RequestMapping(path = EndpointAddresses.DATA_TRANSMISSION, method = RequestMethod.POST)
    public ResponseEntity<String> dataTransmissionMessage(@RequestBody MessageDto message) {

        message.setMessageType("data_transmission");
        messageHandler.inputMessage(message);

        return new ResponseEntity<>("data transmission message received" , HttpStatus.OK);
    }
}
