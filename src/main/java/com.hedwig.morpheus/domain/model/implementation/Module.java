package com.hedwig.morpheus.domain.model.implementation;

import java.util.Objects;

/**
 * Created by hugo. All rights reserved.
 */
public class Module {
    private static final String S_2_M = "s2m";
    private static final String M_2_S = "m2s";

    private String id;
    private String name;
    private String topic;

    public Module() {
    }

    public Module(String id, String name, String topic) {
        this.id = id;
        this.name = name;
        this.topic = topic;
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
        return Objects.equals(id, module.id) &&
               Objects.equals(name, module.name) &&
               Objects.equals(topic, module.topic);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name, topic);
    }

    @Override
    public String toString() {
        return "Module{" + "id='" + id + '\'' + ", name='" + name + '\'' + ", topic='" + topic + '\'' + '}';
    }
}
