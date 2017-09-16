package com.hedwig.morpheus.service.implementation;

import com.hedwig.morpheus.business.Cloud;
import com.hedwig.morpheus.domain.dto.MessageDto;
import com.hedwig.morpheus.domain.implementation.Message;
import com.hedwig.morpheus.domain.implementation.MessageQueue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Scope;
import org.springframework.core.convert.ConversionService;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import java.io.*;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by hugo. All rights reserved.
 */
@Component
@Scope("singleton")
public class BackupMessageService {

    private static final Logger logger = LoggerFactory.getLogger(BackupMessageService.class);
    private static File fileToPersist;
    private final int MAX_POOL_SIZE = 2;
    private final MessageQueue backupMessageQueue;
    private final List<MessageDto> messagesToBePersisted = Collections.synchronizedList(new ArrayList<>());
    private final Map<String, Integer> triesPerMessage = Collections.synchronizedMap(new HashMap<>());
    private final Integer MAX_NUMBER_OF_RESEND_TRIES;
    private final Cloud cloud;
    private final Path backupPath;
    private final Integer persistMessagesEvery;
    private final ConversionService conversionService;
    private final ExecutorService threadPool = Executors.newFixedThreadPool(MAX_POOL_SIZE, new ThreadFactory() {
        private AtomicInteger atomicCounter = new AtomicInteger(1);

        @Override
        public Thread newThread(Runnable r) {
            Thread thread = new Thread(r);
            thread.setName(String.format("BackupThread-%d", atomicCounter.getAndIncrement()));
            thread.setDaemon(true);
            return thread;
        }
    });

    private final Timer scheduler;

    @Autowired
    public BackupMessageService(Environment environment,
                                @Qualifier("backupMessageQueue") MessageQueue backupMessageQueue,
                                Cloud cloud,
                                ConversionService conversionService) throws IOException {
        this.MAX_NUMBER_OF_RESEND_TRIES =
                environment.getProperty("morpheus.configuration.backup.numberOfResendTries", Integer.class);
        if (null == MAX_NUMBER_OF_RESEND_TRIES) {
            throw new IllegalStateException("Property numberOfResendTries not defined in application.yml");
        }

        String directory = environment.getProperty("morpheus.configuration.backup.directory");
        String fileName = environment.getProperty("morpheus.configuration.backup.fileName");

        backupPath = Paths.get(directory, fileName);
        if (null == backupPath) {
            throw new IllegalStateException("Property directory and/or filename not defined in application.yml");
        }

        persistMessagesEvery = environment.getProperty("morpheus.configuration.backup.persistEvery", Integer.class);
        if (null == persistMessagesEvery) {
            throw new IllegalStateException("Property persistEvery not defined in application.yml");
        }

        fileToPersist = new File(backupPath.toUri());
        try {
            fileToPersist.createNewFile();
        } catch (IOException e) {
            logger.error("Unable to create file for message persistance", e);
            throw e;
        }

        this.scheduler = new Timer();
        this.cloud = cloud;
        this.backupMessageQueue = backupMessageQueue;
        this.conversionService = conversionService;

        scheduleMessagePersistence();
    }

    public static List<MessageDto> readSerializedMessagesInFile() {
        List<MessageDto> readMessages;
        if(!fileToPersist.exists()) {
            return new ArrayList<>();
        }
        try (FileInputStream fileInputStream = new FileInputStream(fileToPersist)) {
            final ObjectInputStream objectInputStream = new ObjectInputStream(fileInputStream);
            readMessages = (List<MessageDto>) objectInputStream.readObject();
            logger.info(String.format("A total of %d messages were read from file", readMessages.size()));

            if (null == readMessages) {
                return new ArrayList<>();
            }

            fileToPersist.delete();
            return readMessages;
        } catch (IOException e) {
            logger.error("Unable to create input stream", e);
            return new ArrayList<>();
        } catch (ClassNotFoundException e) {
            logger.error("Could not deserialize objects", e);
            return new ArrayList<>();
        }
    }

    public void startBackupService() {
        threadPool.execute(() -> {
            while (true) {
                Message message = backupMessageQueue.pop();
                threadPool.execute(() -> processMessage(message));
            }
        });
    }

    private void processMessage(Message message) {
        int tries = 1;
        if (triesPerMessage.containsKey(message.getId())) {
            tries = triesPerMessage.get(message.getId());
            if (tries > MAX_NUMBER_OF_RESEND_TRIES) {
                logger.info(String.format("Exceeded amount of tries (%d) to resend message %s",
                                          MAX_NUMBER_OF_RESEND_TRIES,
                                          message.getId()));
                switch (message.getType()) {
                    case CONFIRMATION:
                    case DATA_TRANSMISSION:
                        messagesToBePersisted.add(conversionService.convert(message, MessageDto.class));
                        logger.info(String.format("Message %s will be persisted", message.getId()));
                    default:
                        return;
                }
            }
        }

        logger.info(String.format("Resending message %s. Try (%d / %d)",
                                  message.getId(),
                                  tries,
                                  MAX_NUMBER_OF_RESEND_TRIES));
        tries++;
        triesPerMessage.put(message.getId(), tries);
        cloud.sendMessageToCloud(message);
    }

    private void scheduleMessagePersistence() {
        scheduler.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                if (null != messagesToBePersisted && messagesToBePersisted.size() != 0) {
                    try (FileOutputStream fileOutputStream = new FileOutputStream(fileToPersist)) {
                        final ObjectOutputStream objectOutputStream = new ObjectOutputStream(fileOutputStream);
                        objectOutputStream.writeObject(messagesToBePersisted);
                        objectOutputStream.close();
                        logger.info(String.format("A total of %d messages were serialized to file",
                                                  messagesToBePersisted.size()));
                        messagesToBePersisted.clear();
                    } catch (IOException e) {
                        logger.error("Unable to create output stream", e);
                    }
                }
            }
        }, persistMessagesEvery, persistMessagesEvery);
    }

    public void shutdownExecutorService() {
        threadPool.shutdown();
    }
}
