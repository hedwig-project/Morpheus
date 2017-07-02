package com.hedwig.morpheus.domain.model.abstracts;

/**
 * Created by hugo on 22/05/17. All rights reserved.
 */
public abstract class Component {
    private String name;
    private String type;
    private String requiredPermissions;
    private String location;
    private String topic;

    public Component(String name, String type, String requiredPermissions, String location, String topic) {
        this.name = name;
        this.type = type;
        this.requiredPermissions = requiredPermissions;
        this.location = location;
        this.topic = topic;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getRequiredPermissions() {
        return requiredPermissions;
    }

    public void setRequiredPermissions(String requiredPermissions) {
        this.requiredPermissions = requiredPermissions;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getTopic() {
        return topic;
    }

    public void setTopic(String topic) {
        this.topic = topic;
    }
}
