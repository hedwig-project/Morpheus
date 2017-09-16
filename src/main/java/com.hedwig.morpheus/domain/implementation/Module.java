package com.hedwig.morpheus.domain.implementation;

import com.hedwig.morpheus.domain.enums.QualityOfService;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.io.Serializable;
import java.util.Objects;

/**
 * Created by hugo. All rights reserved.
 */
@Entity
@Table(name = "Module")
public class Module implements Serializable {

    private static final String S_2_M = "s2m";
    private static final String M_2_S = "m2s";

    @Id
    private String id;
    private String name;
    private String topic;
    private QualityOfService qualityOfService;
    private int receiveMessagesAtMostEvery;

    public Module() {
    }

    public Module(String id, String name, String topic) {
        this.id = id;
        this.name = name;
        this.topic = topic;

        this.qualityOfService = QualityOfService.FIRST_LEVEL;
        this.receiveMessagesAtMostEvery = 60;

    }

    public int getReceiveMessagesAtMostEvery() {
        return receiveMessagesAtMostEvery;
    }

    public void setReceiveMessagesAtMostEvery(int receiveMessagesAtMostEvery) {
        this.receiveMessagesAtMostEvery = receiveMessagesAtMostEvery;
    }

    public void configureReceiveMessagesAtMostEvery(String timeString) {
        String[] timeParts = timeString.split(":");

        if (timeParts.length < 2) return;

        int parsedTime;

        try {
            parsedTime = Integer.valueOf(timeParts[0]);
        } catch (NumberFormatException e) {
            return;
        }

        switch (timeParts[1]) {
            case "h":
                setReceiveMessagesAtMostEvery(parsedTime * 3600);
                break;
            case "m":
                setReceiveMessagesAtMostEvery(parsedTime * 60);
            default:
                setReceiveMessagesAtMostEvery(parsedTime);
        }
    }

    public QualityOfService getQualityOfService() {
        return qualityOfService;
    }

    public void setQualityOfService(QualityOfService qualityOfService) {
        this.qualityOfService = qualityOfService;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getTopic() {
        return topic;
    }

    public void setTopic(String topic) {
        this.topic = topic;
    }

    public String getSubscribeToTopic() {
        return this.topic + '/' + M_2_S;
    }

    public String getPublishToTopic() {
        return this.topic + '/' + S_2_M;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Module module = (Module) o;
        return Objects.equals(name, module.name) && Objects.equals(topic, module.topic);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, topic);
    }

    @Override
    public String toString() {
        return "Module{" + "id='" + id + '\'' + ", name='" + name + '\'' + ", topic='" + topic + '\'' + '}';
    }
}
