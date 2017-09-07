package com.hedwig.morpheus.domain.implementation;

import com.hedwig.morpheus.domain.enums.QualityOfService;

import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import static java.util.Objects.hash;

/**
 * Created by hugo. All rights reserved.
 */
public class Message implements Comparable<Message> {
    private final MessageType type;
    private final List<ControlParameter> controlParameters;
    private String id;
    private QualityOfService qosLevel;
    private String topic;
    private MessageBody body;
    private MessagePriority priority;

    public Message(String topic, MessageType type, MessageBody body) {
        this.controlParameters = new LinkedList<>();
        this.type = type;
        this.body = body;
        this.topic = topic;
        this.priority = MessagePriority.NORMAL;

        this.qosLevel = QualityOfService.FIRST_LEVEL;

        createId();
    }

//    TODO : Use ID generator instead of this one. And it should not be created here

    private void createId() {
        int hashValue = Math.abs(Objects.hash(type, body, topic));

        id = String.format("%d%d", hashValue, System.currentTimeMillis());
    }

    @Override
    public int compareTo(Message m) {
        return this.priority.compareTo(m.getPriority());
    }

    public String getId() {
        return id;
    }

    public MessagePriority getPriority() {
        return priority;
    }

    public void setPriority(MessagePriority priority) {
        this.priority = priority;
    }

    public String getTopic() {
        return topic;
    }

    public void setTopic(String topic) {
        this.topic = topic;
    }

    public List<ControlParameter> getControlParameters() {
        return controlParameters;
    }

    public void addControlParameter(ControlParameter controlParameter) {
        controlParameters.add(controlParameter);
    }

    public void removeControlParameter(ControlParameter controlParameter) {
        controlParameters.remove(controlParameter);
    }

    public int getQosLevel() {
        return qosLevel.getQosLevel();
    }

    public void setQosLevel(QualityOfService qosLevel) {
        this.qosLevel = qosLevel;
    }

    public MessageBody getBody() {
        return body;
    }

    public void setBody(MessageBody body) {
        this.body = body;
    }

    private String getControlParametersSection() {
        return controlParameters.stream().map(ControlParameter::toString).collect(Collectors.joining());
    }

    public MessageType getType() {
        return type;
    }

    public String toString() {
        return String.format("%s%s%s", type.toString(), getControlParametersSection(), body.toString());
    }

    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Message message = (Message) o;
        return Objects.equals(topic, message.topic) &&
               type == message.type &&
               Objects.equals(controlParameters, message.controlParameters) &&
               Objects.equals(body, message.body);
    }

    public int hashCode() {
        return hash(topic, type, controlParameters, body);
    }

    public enum MessageType {
        CONFIGURATION("configuration"),
        ACTION_REQUEST("action_request"),
        CONFIRMATION("confirmation"),
        DATA_TRANSMISSION("data_transmission"),
        DATA_REQUEST("data_request");

        private final String headerValue;

        MessageType(String headerValue) {
            this.headerValue = headerValue;
        }

        public String getHeaderValue() {
            return headerValue;
        }

        @Override
        public String toString() {
            return String.format("#%s\n", headerValue);
        }
    }

    public enum MessagePriority {
        HIGH, NORMAL, LOW;
    }

    public static class MessageBody {
        private String payload;

        public MessageBody(String payload) {
            this.payload = payload;
        }

        public String getPayload() {
            return payload;
        }

        public void setPayload(String payload) {
            this.payload = payload;
        }

        @Override
        public String toString() {
            return String.format("@\n%s\n@\n", payload);
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            MessageBody that = (MessageBody) o;
            return Objects.equals(payload, that.payload);
        }

        @Override
        public int hashCode() {
            return hash(payload);
        }
    }

    public static class ControlParameter {
        private String parameter;
        private String value;

        public ControlParameter(String parameter, String value) {
            this.parameter = parameter;
            this.value = value;
        }

        public String getParameter() {
            return parameter;
        }

        public void setParameter(String parameter) {
            this.parameter = parameter;
        }

        public String getValue() {
            return value;
        }

        public void setValue(String value) {
            this.value = value;
        }

        @Override
        public String toString() {
            return String.format("$%s:%s\n", parameter, value);
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            ControlParameter that = (ControlParameter) o;
            return Objects.equals(parameter, that.parameter);
        }

        @Override
        public int hashCode() {
            return hash(parameter);
        }
    }
}
