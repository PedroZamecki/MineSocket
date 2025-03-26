package org.zamecki.minesocket.config;

import com.electronwill.nightconfig.core.*;
import com.electronwill.nightconfig.core.io.WritingMode;
import com.electronwill.nightconfig.toml.TomlFormat;
import com.google.common.base.Predicates;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;

import static org.zamecki.minesocket.ModData.logger;

public abstract class Config implements UnmodifiableConfig, CommentedConfig {
    private CommentedConfig config;
    private final Path path;

    protected Config(Path path) {
        this(path, readToml(path));
    }

    protected Config(Path path, String toml) {
        this.parse(toml);
        this.path = path;
        this.load();
        this.write();
    }

    public <T> T add(String key, T defaultValue, String comment) {
        return this.add(key, defaultValue, comment, Predicates.alwaysTrue());
    }

    public <T> T add(String key, T defaultValue, String comment, Predicate<T> validator) {
        var value = config.getOrElse(key, defaultValue);

        // Revert to default if validation fails
        if (!validator.test(value)) {
            logger.warn("Invalid value {} for key {}. Reverting to default", value, key);
            value = defaultValue;
        }

        config.set(key, value);
        config.setComment(key, comment);
        return value;
    }

    public <T> T getOrAdd(String key, T defaultValue, String comment) {
        T res = this.get(key);
        if (res == null) res = this.add(key, defaultValue, comment);
        return res;
    }

    public abstract void load();

    public void parse(String toml) {
        this.config = TomlFormat.instance().createParser().parse(toml);
    }

    public void read() {
        this.parse(readToml(this.path));
    }

    public void write() {
        TomlFormat.instance().createWriter().write(this.config, this.path, WritingMode.REPLACE);
    }

    private static String readToml(Path path) {
        // Create parent directories as needed
        if (!path.getParent().toFile().mkdirs()) {
            logger.error("Failed to create parent directories for {}", path);
        }

        try {
            return Files.readString(path);
        } catch (Exception ignored) {
        }
        return "";
    }

    @Override
    public <T> T getRaw(List<String> path) {
        return config.getRaw(path);
    }

    @Override
    public Map<String, Object> valueMap() {
        return config.valueMap();
    }

    @Override
    public boolean contains(List<String> path) {
        return config.contains(path);
    }

    @Override
    public int size() {
        return config.size();
    }

    @Override
    public boolean isEmpty() {
        return config.isEmpty();
    }

    @Override
    public boolean equals(Object obj) {
        return config.getClass() == obj.getClass() && config.equals(obj);
    }

    @Override
    public int hashCode() {
        return config.hashCode();
    }

    @Override
    public ConfigFormat<?> configFormat() {
        return config.configFormat();
    }

    @Override
    public <T> T set(List<String> path, Object value) {
        return config.set(path, value);
    }

    @Override
    public boolean add(List<String> path, Object value) {
        return config.add(path, value);
    }

    @Override
    public <T> T remove(List<String> path) {
        return config.remove(path);
    }

    @Override
    public void clear() {
        config.clear();
    }

    @Override
    public String getComment(List<String> path) {
        return config.getComment(path);
    }

    @Override
    public boolean containsComment(List<String> path) {
        return config.containsComment(path);
    }

    @Override
    public String setComment(List<String> path, String comment) {
        return config.setComment(path, comment);
    }

    @Override
    public String removeComment(List<String> path) {
        return config.removeComment(path);
    }

    @Override
    public Map<String, String> commentMap() {
        return config.commentMap();
    }

    @Override
    public Set<? extends CommentedConfig.Entry> entrySet() {
        return config.entrySet();
    }

    @Override
    public void clearComments() {
        config.clearComments();
    }

    @Override
    public void putAllComments(Map<String, UnmodifiableCommentedConfig.CommentNode> comments) {
        config.putAllComments(comments);
    }

    @Override
    public void putAllComments(UnmodifiableCommentedConfig commentedConfig) {
        config.putAllComments(commentedConfig);
    }

    @Override
    public Map<String, UnmodifiableCommentedConfig.CommentNode> getComments() {
        return config.getComments();
    }

    @Override
    public CommentedConfig createSubConfig() {
        return config.createSubConfig();
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + ':' + config;
    }
}
