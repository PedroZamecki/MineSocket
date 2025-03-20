package org.zamecki.minesocket.controller;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

import static org.zamecki.minesocket.ModData.MOD_ID;
import static org.zamecki.minesocket.ModData.logger;

public class SettingsController {
    private final Path settingsPath;
    private final Map<String, String> currentSettings = new HashMap<>();

    public SettingsController() {
        logger.info("SettingsController is initializing");
        settingsPath = Paths.get("settings/" + MOD_ID + "/settings");
        createSettingsDirectory();
        loadAllSettingsFiles();
    }

    private void createSettingsDirectory() {
        try {
            Files.createDirectories(settingsPath);
            logger.info("Settings directory created");
        } catch (IOException e) {
            logger.error("Error creating settings directory", e);
        }
    }

    private void loadAllSettingsFiles() {
        try (var stream = getClass().getResourceAsStream("/assets/" + MOD_ID + "/settings")) {
            if (stream == null) {
                logger.error("Resource path not found");
                return;
            }
            new BufferedReader(new InputStreamReader(stream))
                .lines()
                .filter(line -> line.endsWith(".json"))
                .forEach(this::loadSettingsFile);
        } catch (IOException e) {
            logger.error("Error loading settings files", e);
        }
    }

    private void loadSettingsFile(String fileName) {
        try (InputStream stream = getClass().getResourceAsStream("/assets/" + MOD_ID + "/settings/" + fileName)) {
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
