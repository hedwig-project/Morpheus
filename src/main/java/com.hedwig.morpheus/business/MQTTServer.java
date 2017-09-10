package com.hedwig.morpheus.business;

import com.hedwig.morpheus.domain.implementation.Message;
import com.hedwig.morpheus.domain.implementation.MessageQueue;
import com.hedwig.morpheus.domain.implementation.Result;
import com.hedwig.morpheus.domain.interfaces.IServer;
import com.hedwig.morpheus.security.Securities;
import org.eclipse.paho.client.mqttv3.*;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Scope;
import org.springframework.core.convert.ConversionService;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import javax.net.ssl.SSLSocketFactory;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

// TODO : Make this code all asynchronous
// TODO : Make a server interface
// TODO : Inversion of control, message manager should have an instance of the server
// TODO : Add logging functionality
// TODO : Add subscription security
// TODO : Add configuration file
// TODO : Add QoS according to message type

/**
 * Created by hugo. All rights reserved.
 */
@Component
@Scope("singleton")
public class MQTTServer implements IServer {
    private static final int MAX_POOL_SIZE = 10;

    private final String id;
    private final String host;
    private final int port;
    private final String serialNumber;
    private final String password;

    private final MqttAsyncClient mqttAsyncClient;
    private final MqttConnectOptions mqttConnectOptions;
    private final MemoryPersistence memoryPersistence;

    private final Path caCertificate;
    private final Path serverCertificate;
    private final Path serverKey;

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final ExecutorService sendersThreadPool = Executors.newFixedThreadPool(MAX_POOL_SIZE, new ThreadFactory() {
        private AtomicInteger atomicCounter = new AtomicInteger(1);

        @Override
        public Thread newThread(Runnable r) {
            Thread thread = new Thread(r);
            thread.setName(String.format("MQTTSenderThread-%d", atomicCounter.getAndIncrement()));
            thread.setDaemon(true);
            return thread;
        }
    });

    private final ExecutorService receiversThreadPool =
            Executors.newFixedThreadPool(MAX_POOL_SIZE, new ThreadFactory() {
                private AtomicInteger atomicCounter = new AtomicInteger(1);

                @Override
                public Thread newThread(Runnable r) {
                    Thread thread = new Thread(r);
                    thread.setName(String.format("MQTTReceiverThread-%d", atomicCounter.getAndIncrement()));
                    thread.setDaemon(true);
                    return thread;
                }
            });

    private final ConversionService conversionService;

    private final MessageQueue incomeMessageQueue;
    private final MessageQueue outputMessageQueue;
    private final MessageQueue backupMessageQueue;

    SSLSocketFactory socketFactory;

    IMqttToken mqttConnectToken;

    @Autowired
    private MQTTServer(@Qualifier("incomeMessageQueue") MessageQueue incomeMessageQueue,
                       @Qualifier("outputMessageQueue") MessageQueue outputMessageQueue,
                       @Qualifier("backupMessageQueue") MessageQueue backupMessageQueue,
                       Environment environment,
                       ConversionService conversionService) throws Exception {

        this.id = environment.getProperty("mqttServer.configuration.id");
        this.port = Integer.parseInt(environment.getProperty("mqttServer.configuration.port"));
        this.host = environment.getProperty("mqttServer.configuration.host");
        this.serialNumber = environment.getProperty("morpheus.configuration.serialNumber");
        this.password = environment.getProperty("morpheus.configuration.password");

        this.incomeMessageQueue = incomeMessageQueue;
        this.outputMessageQueue = outputMessageQueue;
        this.backupMessageQueue = backupMessageQueue;

        this.caCertificate = Paths.get(environment.getProperty("mqttServer.certificate.caCertificate"));
        this.serverCertificate = Paths.get(environment.getProperty("mqttServer.certificate.serverCertificate"));
        this.serverKey = Paths.get(environment.getProperty("mqttServer.certificate.serverKey"));

        this.conversionService = conversionService;

        mqttConnectOptions = new MqttConnectOptions();
        mqttConnectOptions.setMqttVersion(MqttConnectOptions.MQTT_VERSION_3_1_1);
        mqttConnectOptions.setKeepAliveInterval(MqttConnectOptions.KEEP_ALIVE_INTERVAL_DEFAULT);

        memoryPersistence = new MemoryPersistence();

        mqttAsyncClient = new MqttAsyncClient(getConnectionUrl(), id, memoryPersistence);

        socketFactory = Securities.createSocketFactory(this.caCertificate.toString(),
                                                       this.serverCertificate.toString(),
                                                       this.serverKey.toString());

//        mqttConnectOptions.setSocketFactory(socketFactory);

        mqttConnectOptions.setUserName(serialNumber);
        mqttConnectOptions.setPassword(password.toCharArray());

        setCallBack();
        startMessageSending();
    }

    private void setCallBack() {
        mqttAsyncClient.setCallback(new MqttCallback() {
            @Override
            public void connectionLost(Throwable cause) {
                logger.error("Connection was lost. " + cause.getCause());
                System.exit(0);
            }

            @Override
            public void messageArrived(String topic, MqttMessage mqttMessage) throws Exception {
                // TODO : Make actual parsing of the message
                receiversThreadPool.execute(() -> {
                    logger.info("New message received from module: " + topic);

                    Message message = conversionService.convert(mqttMessage, Message.class);

                    if (null == message) {
                        logger.error("Could not convert message", new IllegalStateException("Invalid message"));
                        return;
                    }

                    message.setTopic(topic);
                    incomeMessageQueue.push(message);
                });
            }

            @Override
            public void deliveryComplete(IMqttDeliveryToken token) {

            }
        });
    }

//    @Override
//    public String getConnectionUrl() {
//        return String.format("ssl://%s:%d", host, port);
//    }

    @Override
    public String getConnectionUrl() {
        return String.format("tcp://%s:%d", host, port);
    }

    @Override
    public void connect() throws MqttException {
        mqttConnectToken = mqttAsyncClient.connect(mqttConnectOptions, null, null);
        mqttConnectToken.waitForCompletion();
    }

    private void startMessageSending() {
        Thread senderThread = new Thread(() -> {
            while (true) {
                Message message = outputMessageQueue.pop();
                sendersThreadPool.execute(() -> {
                    MqttMessage mqttMessage = new MqttMessage(message.toString().getBytes());
                    mqttMessage.setQos(message.getQosLevel());

                    IMqttActionListener pubListener = new IMqttActionListener() {
                        @Override
                        public void onSuccess(IMqttToken asyncActionToken) {
                            logger.info(String.format("Message %s successfully sent", message.getId()));
                        }

                        @Override
                        public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                            logger.info(String.format("Message %s could not be sent and was stored in the backup queue",
                                                      message.getId()));
                            backupMessageQueue.push(message);
                        }
                    };

                    try {
                        mqttAsyncClient.publish(message.getTopic(), mqttMessage, null, pubListener);
                    } catch (MqttException e) {
                        logger.error("Error publishing message", e);
                    }
                });
            }
        });

        senderThread.setName("SenderThread");
        senderThread.start();
    }


    @Override
    public void shutdown() {
        sendersThreadPool.shutdown();
        receiversThreadPool.shutdown();
        try {
            mqttAsyncClient.disconnect();
            logger.info("Server successfully disconnected");
        } catch (MqttException e) {
            logger.error("Could not disconnect properly", e);
        }
    }

    @Override
    public Result subscribe(String topic) {
        Result result = new Result();
        try {
            IMqttToken token = mqttAsyncClient.subscribe(topic, 0);
            token.waitForCompletion();
            int grantedQos = token.getGrantedQos()[0];

            switch (grantedQos) {
                case 0:
                case 1:
                case 2:
                    result.setSuccess(true);
                    result.setDescription("Subscribed to topic successfully");
                    break;
                default:
                    result.setSuccess(false);
                    result.setDescription("Failed to subscribe to topic");
            }

            return result;

        } catch (MqttException e) {
            logger.error("Error in subscription", e);
            result.setSuccess(false);
            result.setDescription("Failed to subscribe to topic due to MqttException");
            return result;
        }
    }

    @Override
    public Result unsubscribe(String topic) {
        Result result = new Result();
        try {
            IMqttToken token = mqttAsyncClient.unsubscribe(topic);
            token.waitForCompletion();
            result.setSuccess(true);
            result.setDescription("Unsubscribed from topic successfully");
        } catch (MqttException e) {
            logger.error("Error in subscription", e);
            result.setSuccess(false);
            result.setDescription("Failed to unsubscribe from topic due to MqttException");
        }

        return result;
    }

//    @Override
//    public void subscribe(String topic, Runnable successfullySubscribed, Runnable failureInSubscription) {
//        try {
//            mqttAsyncClient.subscribe(topic, 0, null, new IMqttActionListener() {
//                @Override
//                public void onSuccess(IMqttToken asyncActionToken) {
//                    successfullySubscribed.run();
//                }
//
//                @Override
//                public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
//                    failureInSubscription.run();
//                }
//            });
//        } catch (MqttException e) {
//            logger.error("Error in subscription", e);
//        }
//    }

//    @Override
//    public boolean unsubscribe(String topic) {
//        try {
//            IMqttToken token = mqttAsyncClient.unsubscribe(topic);
//            token.waitForCompletion();
//            return token.getResponse().toString().equals("UNSUBACK msgId 4");
//        } catch (MqttException e) {
//            logger.error("Error in subscription", e);
//        }
//
//        return false;
//    }
}
