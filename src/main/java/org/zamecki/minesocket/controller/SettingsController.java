package org.zamecki.minesocket.controller;

import org.slf4j.Logger;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

public class SettingsController {
    private final Path settingsPath;
    private final Logger logger;
    private final Map<String, String> currentSettings = new HashMap<>();

    public SettingsController(String modId, Logger _logger) {
        logger = _logger;
        logger.info("SettingsController is initializing");
        settingsPath = Paths.get("settings/" + modId + "/settings");
        createSettingsDirectory();
        loadAllSettingsFiles(modId);
    }

    private void createSettingsDirectory() {
        try {
            Files.createDirectories(settingsPath);
            logger.info("Settings directory created");
        } catch (IOException e) {
            logger.error("Error creating settings directory", e);
        }
    }

    private void loadAllSettingsFiles(String modId) {
        try (var stream = getClass().getResourceAsStream("/assets/" + modId + "/settings")) {
            if (stream == null) {
                logger.error("Resource path not found");
                return;
            }
            new BufferedReader(new InputStreamReader(stream))
                .lines()
                .filter(line -> line.endsWith(".json"))
                .forEach(fileName -> loadSettingsFile(modId, fileName));
        } catch (IOException e) {
            logger.error("Error loading settings files", e);
        }
    }

    private void loadSettingsFile(String modId, String fileName) {
        try (InputStream stream = getClass().getResourceAsStream("/assets/" + modId + "/settings/" + fileName)) {
            if (stream == null) {
                logger.error("Resource path not found for file: {}", fileName);
                return;
            }

            try (BufferedReader reader = new BufferedReader(new InputStreamReader(stream))) {
                reader.lines().forEach(line -> {
                    String[] parts = line.split("=", 2);
                    if (parts.length == 2) {
                        currentSettings.put(parts[0].trim(), parts[1].trim());
                    }
                });
                logger.info("Settings file loaded: {}", fileName);
            }
        } catch (IOException e) {
            logger.error("Error loading settings file: {}", fileName, e);
        }
    }

    public Map<String, String> getCurrentSettings() {
        return currentSettings;
    }
}
