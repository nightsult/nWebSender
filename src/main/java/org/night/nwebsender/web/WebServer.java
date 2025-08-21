package org.night.nwebsender.web;

import com.mojang.logging.LogUtils;
import com.sun.net.httpserver.HttpServer;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.server.MinecraftServer;
import net.neoforged.neoforge.server.ServerLifecycleHooks;
import org.slf4j.Logger;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Executors;

public class WebServer {
    private static final Logger LOGGER = LogUtils.getLogger();

    private final HttpServer server;
    private final int port;
    private final String secretKey;
    private final List<String> allowedIps;

    public WebServer(int port, String secretKey, String[] allowedIps) throws IOException {
        this.port = port;
        this.secretKey = secretKey;
        this.allowedIps = Arrays.asList(allowedIps);

        LOGGER.info("Web server initialized on port {}", port);
        server = HttpServer.create(new InetSocketAddress(port), 0);
        server.createContext("/execute", new RequestHandler(this));
        server.setExecutor(Executors.newCachedThreadPool());
    }

    public void start() {
        server.start();
    }

    public void stop() {
        server.stop(0);
        try {
            Thread.sleep(200);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    public String getSecretKey() {
        return secretKey;
    }

    public List<String> getAllowedIps() {
        return allowedIps;
    }

    public boolean executeCommand(String command) {
        MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
        if (server == null) {
            LOGGER.error("Server not found when executing command: {}", command);
            return false;
        }

        CommandSourceStack commandSource = server.createCommandSourceStack();
        server.getCommands().performPrefixedCommand(commandSource, command);
        return true;
    }
}