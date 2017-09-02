package com.hedwig.morpheus.rest;

import com.hedwig.morpheus.rest.model.MessageDto;
import com.hedwig.morpheus.rest.model.configuration.ConfigurationDto;
import com.hedwig.morpheus.rest.service.RestConfigurationHandler;
import com.hedwig.morpheus.rest.service.RestMessageHandler;
import com.hedwig.morpheus.rest.service.RestMessageRequestsHandlers;
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

    @Autowired
    RestMessageHandler messageHandler;

    @Autowired
    RestConfigurationHandler configurationHandler;

    @Autowired
    RestMessageRequestsHandlers restMessageRequestsHandlers;

    @RequestMapping(path = EndpointAddresses.CONFIGURATION, method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<String> configurationMessage(@RequestBody ConfigurationDto configuration) {

        configurationHandler.inputNewConfiguration(configuration);

        return new ResponseEntity<>("configuration message received", HttpStatus.OK);
    }

    @RequestMapping(path = EndpointAddresses.ACTION_REQUEST, method = RequestMethod.POST)
    public ResponseEntity<String> actionRequestMessage(@RequestBody List<MessageDto> messages) {

        restMessageRequestsHandlers.inputRequestMessages(messages, "action_request");

        return new ResponseEntity<>("action request message sent to module", HttpStatus.OK);
    }

    @RequestMapping(path = EndpointAddresses.DATA_TRANSMISSION, method = RequestMethod.POST)
    public ResponseEntity<String> dataTransmissionMessage(@RequestBody List<MessageDto> messages) {

        restMessageRequestsHandlers.inputRequestMessages(messages, "data_transmission");

        return new ResponseEntity<>("data transmission message sent to module" , HttpStatus.OK);
    }
}
