package com.hedwig.service;

import com.hedwig.morpheus.EntryPoint;
import com.hedwig.morpheus.business.MQTTServer;
import com.hedwig.morpheus.domain.implementation.Result;
import com.hedwig.morpheus.service.implementation.TopicManager;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Created by hugo. All rights reserved.
 */

@RunWith(SpringRunner.class)
@SpringBootTest(classes = EntryPoint.class)
public class TopicManagerTest {

    @Mock
    private MQTTServer mqttServer;

    @InjectMocks
    private TopicManager topicManager;

    @Before
    public void setupMocks() {
        Result result = new Result();
        result.setSuccess(true);

        // Subscription
        Mockito.when(mqttServer.subscribe(Mockito.anyString())).thenReturn(result);

        // Unsubscribe
        Mockito.when(mqttServer.unsubscribe(Mockito.anyString())).thenReturn(result);
    }

    @Test
    public void subscribeToTopic() {
        String topic = "hw/kitchen/s2m";
        topicManager.subscribe(topic);

        assertTrue(topicManager.isSubscribed(topic));
    }

    @Test
    public void tryToUnsubscribeUnkownTopic() {
        String topic = "hw/kitchen/s2m";
        assertFalse(topicManager.unsubscribe(topic).isSuccess());
    }

    @Test
    public void unsubscribeFromTopic() {
        String topic = "hw/kitchen/s2m";
        topicManager.subscribe(topic);

        assertTrue(topicManager.isSubscribed(topic));

        assertTrue(topicManager.unsubscribe(topic).isSuccess());

        assertFalse(topicManager.isSubscribed(topic));
    }
}
