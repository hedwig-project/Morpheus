package com.hedwig.morpheus.service.implementation;

import com.hedwig.morpheus.domain.implementation.Module;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import java.io.*;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

/**
 * Created by hugo. All rights reserved.
 */
@Component
@Scope("singleton")
public class ConfigurationPersister {
    private static final Logger logger = LoggerFactory.getLogger(ConfigurationPersister.class);
    private final Set<Module> modules;
    private final String directoryName;
    private final String fileName;
    private final Path directory;
    private final File file;

    @Autowired
    public ConfigurationPersister(Environment environment) throws IOException, IllegalAccessException {
        directoryName = environment.getProperty("morpheus.configuration.directory");
        if (null == directoryName) {
            logger.error("No configuration directory found in properties");
            throw new IllegalStateException();
        }

        modules = Collections.synchronizedSet(new HashSet<>());
        fileName = "modules.mps";
        directory = Paths.get(directoryName, fileName);
        file = new File(directory.toUri());
        if (!file.exists()) {
            file.createNewFile();
        }
        if (!file.canWrite()) {
            logger.error("Unable to write in configuration file");
            throw new IllegalAccessException();
        }

        retrieveModules();
    }

    public void updateModulesList(Module module) {
        modules.add(module);
        persistModules();
    }

    public Set<Module> getModules() {
        return new HashSet<>(modules);
    }

    private void persistModules() {
        if (modules.size() != 0) {
            try (FileOutputStream fileOutputStream = new FileOutputStream(file)) {
                final ObjectOutputStream objectOutputStream = new ObjectOutputStream(fileOutputStream);
                objectOutputStream.writeObject(modules);
                objectOutputStream.close();
            } catch (IOException e) {
                logger.error("Unable to create output stream", e);
            }
        } else {
            file.delete();
        }
    }

    private void retrieveModules() {
        try (FileInputStream fileInputStream = new FileInputStream(file)) {
            final ObjectInputStream objectInputStream = new ObjectInputStream(fileInputStream);
            Set<Module> foundModules = (Set<Module>) objectInputStream.readObject();
            logger.info(String.format("A total of %d modules were read from file", foundModules.size()));
            modules.addAll(foundModules);
        } catch (EOFException e) {
            logger.info("No modules to load from file");
        } catch (IOException e) {
            logger.error("Unable to create input stream", e);
        } catch (ClassNotFoundException e) {
            logger.error("Could not deserialize objects", e);
        }
    }

    public void removeModule(Module module) {
        modules.remove(module);
        persistModules();
    }
}
