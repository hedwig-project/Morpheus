package com.hedwig.morpheus.domain.model.implementation;

import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Created by hugo. All rights reserved.
 */
public class Message implements Comparable<Message> {
    private int id;
    private QualityOfService qosLevel;
    private final String topic;
    private final MessageType type;
    private final List<ControlParameter> controlParameters;
    private MessageBody body;
    private MessagePriority priority;

    public Message(String topic, MessageType type, MessageBody body) {
        this.controlParameters = new LinkedList<>();
        this.type = type;
        this.body = body;
        this.topic = topic;
        this.priority = MessagePriority.NORMAL;

        this.qosLevel = QualityOfService.FIRST_LEVEL;
    }

    @Override
    public int compareTo(Message m) {
        return this.priority.compareTo(m.getPriority());
    }

    public enum MessageType {
        CONFIGURATION(true, "configuration"),
        ACTION_REQUEST(true, "action_request"),
        CONFIRMATION(false, "confirmation"),
        DATA_TRANSMISSION(false, "data_transmission"),
        DATA_REQUEST(false, "data_request");

        private final boolean serverAsOrigin;
        private final String headerValue;

        MessageType(boolean serverAsOrigin, String headerValue) {
            this.serverAsOrigin = serverAsOrigin;
            this.headerValue = headerValue;
        }

        public boolean isServerAsOrigin() {
            return serverAsOrigin;
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
        HIGH,
        NORMAL,
        LOW;
    }

    public enum QualityOfService {
        FIRST_LEVEL(1),
        SECOND_LEVEL(2),
        THIRD_LEVEL(3);

        private final int qosLevel;

        QualityOfService(int qosLevel) {
            this.qosLevel = qosLevel;
        }

        public int getQosLevel() {
            return qosLevel;
        }
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
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
        return Objects.hash(topic, type, controlParameters, body);
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
            return Objects.hash(payload);
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
            return Objects.hash(parameter);
        }
    }
}
