package com.hedwig.domain;

import com.hedwig.morpheus.EntryPoint;
import com.hedwig.morpheus.domain.enums.MessageType;
import com.hedwig.morpheus.domain.implementation.Message;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;

/**
 * Created by hugo. All rights reserved.
 */

@RunWith(SpringRunner.class)
@SpringBootTest(classes = EntryPoint.class)
public class MessageTest {
    @Test
    public void payloadConfigurationMessage() {
        // given
        Map<String, String> payloadMap = new HashMap<>();
        payloadMap.put("vl", "5");

        Message message = new Message("hw/kitchen", MessageType.CONFIGURATION, new Message.MessageBody(payloadMap));
        message.addControlParameter(new Message.ControlParameter("ts", new Date(new Long("1497209392924")).toString()));
        message.addControlParameter(new Message.ControlParameter("ty", "timeAddition"));

        // when
        String payload = message.toString();


        //then
        String expected = "#configuration\n" +
                         "$ts:Sun Jun 11 16:29:52 BRT 2017\n" +
                         "$ty:timeAddition\n" +
                         "@\n" +
                         "vl:5\n" +
                         "@\n";

        assertEquals(expected, payload);
    }

    @Test
    public void payloadConfigurationMessageWithoutControlParameter() {
        // given
        Map<String, String> payloadMap = new HashMap<>();
        payloadMap.put("vl", "5");

        Message message = new Message("hw/kitchen", MessageType.CONFIGURATION, new Message.MessageBody(payloadMap));

        // when
        String payload = message.toString();

        //then
        assertEquals("#configuration\n" + "@\n" + "vl:5\n" + "@\n", payload);
    }

    @Test
    public void payloadConfigurationMessageWithoutBody() {
        // given
        Map<String, String> payloadMap = new HashMap<>();
        Message message = new Message("hw/kitchen", MessageType.CONFIGURATION, new Message.MessageBody(payloadMap));

        // when
        String payload = message.toString();

        //then
        assertEquals("#configuration\n" + "@\n\n" + "@\n", payload);
    }
}
