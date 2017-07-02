package com.hedwig.morpheus.domain.model.implementation;//package com.hedwig.morpheus.domain.model.implementation;
//
//import com.hedwig.morpheus.security.Securities;
//import com.sun.istack.internal.NotNull;
//import org.eclipse.paho.client.mqttv3.*;
//import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;
//
//import javax.net.ssl.SSLSocketFactory;
//import java.nio.file.Path;
//import java.util.ArrayList;
//import java.util.List;
//
///**
// * Created by hugo. All rights reserved.
// */
//public class Broker {
//
//    private final String id;
//    private final String name;
//    private final int port;
//
//    private final MqttAsyncClient mqttAsyncClient;
//    private final MqttConnectOptions mqttConnectOptions;
//    private final MemoryPersistence memoryPersistence;
//
//    private final Path caCertificate;
//    private final Path brokerCertificate;
//    private final Path brokerKey;
//
//    SSLSocketFactory socketFactory;
//
//    IMqttToken mqttConnectToken;
//
//    private Broker(String name, int port, Path caCertificate, Path brokerCertificate, Path brokerKey) throws Exception {
//
//        this.caCertificate = caCertificate;
//        this.brokerCertificate = brokerCertificate;
//        this.brokerKey = brokerKey;
//
//        this.id = MqttAsyncClient.generateClientId();
//
//        this.name = name;
//        this.port = port;
//
//        mqttConnectOptions = new MqttConnectOptions();
//        mqttConnectOptions.setMqttVersion(MqttConnectOptions.MQTT_VERSION_3_1_1);
//        mqttConnectOptions.setKeepAliveInterval(MqttConnectOptions.KEEP_ALIVE_INTERVAL_DEFAULT);
//
//        memoryPersistence = new MemoryPersistence();
//
//        mqttAsyncClient = new MqttAsyncClient(getConnectionUrl(), id, memoryPersistence);
//
//        socketFactory = Securities.createSocketFactory(this.caCertificate.toString(),
//                                                       this.brokerCertificate.toString(),
//                                                       this.brokerKey.toString());
//
//        mqttConnectOptions.setSocketFactory(socketFactory);
//
//        setCallBack();
//    }
//
//    private void setCallBack() {
//        mqttAsyncClient.setCallback(new MqttCallback() {
//            @Override
//            public void connectionLost(Throwable cause) {
//                System.err.println("Connection was lost");
//                System.exit(0);
//            }
//
//            @Override
//            public void messageArrived(String topic, MqttMessage mqttMessage) throws Exception {
//                //Sensor sensor = new Sensor("Generic sensor", null, null, null, null);
//                Message message = new Message();
//                //message.setFrom(sensor);
//                message.setPayload(new String(mqttMessage.getPayload(), "UTF-8"));
//
//                System.out.println("A new message has arrived: " + message.getPayload());
//            }
//
//            @Override
//            public void deliveryComplete(IMqttDeliveryToken token) {
//
//            }
//        });
//    }
//
//    public static Broker build(String mqttServerHost,
//                               int mqttServerPort,
//                               Path caCertificate,
//                               Path clientCertificate,
//                               Path clientKey) {
//        try {
//            return new Broker(mqttServerHost, mqttServerPort, caCertificate, clientCertificate, clientKey);
//        } catch (Exception e) {
//            throw new IllegalStateException("Broker could not be created.", e);
//        }
//    }
//
//    public String getConnectionUrl() {
//        return String.format("ssl://%s:%d", name, port);
//    }
//
//    public void connect() throws MqttException {
//        mqttConnectToken = mqttAsyncClient.connect(mqttConnectOptions, null, null);
//        mqttConnectToken.waitForCompletion();
//    }
//
//    public boolean sendMessage(Message message) {
//        MqttMessage mqttMessage = new MqttMessage(message.toString().getBytes());
//        try {
//            IMqttDeliveryToken deliveryToken =
//                    mqttAsyncClient.publish(message.getTopic(), mqttMessage, null, null);
//            deliveryToken.waitForCompletion();
//        } catch (MqttException e) {
//            System.err.println("Could not send message.");
//            return false;
//        }
//
//        return true;
//    }
//
//    public boolean subscribe(@NotNull List<String> topicList) {
//        List<IMqttToken> tokenList = new ArrayList<>();
//
//        for(String topic : topicList) {
//            try {
//                IMqttToken mqttToken = mqttAsyncClient.subscribe(topic, 0, null, new IMqttActionListener() {
//                    @Override
//                    public void onSuccess(IMqttToken asyncActionToken) {
//
//                    }
//
//                    @Override
//                    public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
//
//                    }
//                });
//
//                tokenList.add(mqttToken);
//            } catch (MqttException e) {
//                e.printStackTrace();
//                return false;
//            }
//        }
//
//        for (IMqttToken mqttToken : tokenList) {
//            try {
//                mqttToken.waitForCompletion();
//            } catch (MqttException e) {
//                e.printStackTrace();
//                return false;
//            }
//        }
//
//        return true;
//    }
//}
