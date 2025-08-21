package org.night.nwebsender.config;

import com.electronwill.nightconfig.core.CommentedConfig;
import com.electronwill.nightconfig.core.file.CommentedFileConfig;
import com.electronwill.nightconfig.core.io.WritingMode;
import org.apache.commons.lang3.RandomStringUtils;

import java.nio.file.Files;
import java.nio.file.Path;

public class NWebsenderConfig {
    public final int port;
    public final String secretKey;
    public final boolean logRequests;
    public final String allowedIps;

    private NWebsenderConfig(int port, String secretKey, boolean logRequests, String allowedIps) {
        this.port = port;
        this.secretKey = secretKey;
        this.logRequests = logRequests;
        this.allowedIps = allowedIps;
    }

    public static NWebsenderConfig loadOrCreate(Path file) {
        try {
            Files.createDirectories(file.getParent());
            CommentedFileConfig cfg = CommentedFileConfig.builder(file)
                    .sync().autosave().writingMode(WritingMode.REPLACE).build();
            cfg.load();

            CommentedConfig config = cfg.get("NWebsender");
            if (config == null) {
                config = CommentedConfig.inMemory();
                cfg.set("NWebsender", config);
            }

            putIfAbsent(config, "port", 8080);
            putIfAbsent(config, "secretKey", RandomStringUtils.randomAlphanumeric(32));
            putIfAbsent(config, "logRequests", true);
            putIfAbsent(config, "allowedIps", "127.0.0.1,localhost");

            cfg.save();

            int port = getInt(config, "port", 8080);
            String secretKey = getString(config, "secretKey", RandomStringUtils.randomAlphanumeric(32));
            boolean logRequests = getBoolean(config, "logRequests", true);
            String allowedIps = getString(config, "allowedIps", "127.0.0.1,localhost");

            return new NWebsenderConfig(port, secretKey, logRequests, allowedIps);
        } catch (Exception ex) {
            throw new RuntimeException("Failed to load nwebsender-config.toml", ex);
        }
    }

    private static void putIfAbsent(CommentedConfig c, String key, Object value) {
        if (!c.contains(key)) c.add(key, value);
    }

    private static String getString(CommentedConfig c, String key, String def) {
        Object v = c.get(key);
        return v == null ? def : v.toString();
    }

    private static int getInt(CommentedConfig c, String key, int def) {
        Object v = c.get(key);
        if (v instanceof Number n) return n.intValue();
        if (v instanceof String s) try { return Integer.parseInt(s.trim()); } catch (NumberFormatException ignored) {}
        return def;
    }

    private static boolean getBoolean(CommentedConfig c, String key, boolean def) {
        Object v = c.get(key);
        if (v instanceof Boolean b) return b;
        if (v instanceof String s) return Boolean.parseBoolean(s);
        return def;
    }
}