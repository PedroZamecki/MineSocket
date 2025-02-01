package org.zamecki.minesocket.controller;

import org.slf4j.Logger;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

public class LangController {
    Path configPath;
    Logger logger;

    public LangController(String modId, Logger _logger) {
        logger = _logger;
        logger.info("LangController is initializing");
        configPath = Paths.get("config/" + modId + "/lang");
        createConfigDirectory();
        copyAllLanguageFiles(modId);
    }

    private void createConfigDirectory() {
        try {
            Files.createDirectories(configPath);
            logger.info("Config directory created");
        } catch (IOException e) {
            logger.error("Error creating config directory", e);
        }
    }

    private void copyAllLanguageFiles(String modId) {
        getLanguageFilesToCopy(modId, logger).forEach(path -> copyLanguageFile(modId, path));
    }

    private List<Path> getLanguageFilesToCopy(String modId, Logger logger) {
        try (var stream = getClass().getResourceAsStream("/assets/" + modId + "/lang")) {
            if (stream == null) {
                logger.error("Resource path not found");
                return List.of();
            }
            return new BufferedReader(new InputStreamReader(stream))
                .lines()
                .filter(line -> line.endsWith(".json"))
                .filter(path -> !Files.exists(configPath.resolve(path)))
                .map(Paths::get)
                .toList();
        } catch (IOException e) {
            logger.error("Error getting language files", e);
            return List.of();
        }
    }

    private void copyLanguageFile(String modId, Path filePath) {
        try (InputStream stream = getClass().getResourceAsStream("/assets/" + modId + "/lang/" + filePath.getFileName())) {
            if (stream == null) {
                logger.error("Resource path not found");
                return;
            }

            Files.copy(stream, configPath.resolve(filePath));
            logger.info("Language file copied: {}", filePath);
        } catch (IOException e) {
            logger.error("Error copying language file", e);
        }
    }
}
