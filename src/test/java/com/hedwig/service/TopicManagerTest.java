package com.hedwig.service;

import com.hedwig.morpheus.EntryPoint;
import com.hedwig.morpheus.domain.model.implementation.MQTTServer;
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
        // Subscription
        Mockito.doAnswer(invocation -> {
            Runnable success = (Runnable) invocation.getArguments()[1];
            success.run();
            return null;
        }).when(mqttServer).subscribe(Mockito.anyString(), Mockito.any(Runnable.class), Mockito.any(Runnable.class));

        // Unsubscribe
        Mockito.when(mqttServer.unsubscribe(Mockito.anyString())).thenReturn(true);
    }

    @Test
    public void subscribeToTopic() {
        String topic = "hw/kitchen/s2m";
        topicManager.subscribe(topic, "kitchen", null);

        assertTrue(topicManager.isSubscribed(topic));
    }

    @Test
    public void tryToUnsubscribeUnkownTopic() {
        String topic = "hw/kitchen/s2m";
        assertFalse(topicManager.unsubscribe(topic));
    }

    @Test
    public void unsubscribeFromTopic() {
        String topic = "hw/kitchen/s2m";
        topicManager.subscribe(topic, "kitchen", null);

        assertTrue(topicManager.isSubscribed(topic));

        assertTrue(topicManager.unsubscribe(topic));

        assertFalse(topicManager.isSubscribed(topic));
    }
}
