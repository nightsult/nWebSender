package org.night.nwebsender.web;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.mojang.logging.LogUtils;
import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import org.night.nwebsender.Nwebsender;
import org.slf4j.Logger;

import java.io.*;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

public class RequestHandler implements HttpHandler {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final Gson GSON = new Gson();

    private final WebServer webServer;

    public RequestHandler(WebServer webServer) {
        this.webServer = webServer;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        if (Nwebsender.CONFIG.logRequests) {
            LOGGER.info("Received request from {}", exchange.getRemoteAddress().getHostString());
        }

        if (!isIpAllowed(exchange.getRemoteAddress())) {
            sendResponse(exchange, 403, "IP not allowed");
            return;
        }

        if (!"POST".equals(exchange.getRequestMethod())) {
            sendResponse(exchange, 405, "Method not allowed");
            return;
        }

        String requestBody = new BufferedReader(
                new InputStreamReader(exchange.getRequestBody(), StandardCharsets.UTF_8))
                .lines()
                .collect(Collectors.joining("\n"));

        try {
            JsonObject json = GSON.fromJson(requestBody, JsonObject.class);

            // Verify auth key
            if (!json.has("key") || !webServer.getSecretKey().equals(json.get("key").getAsString())) {
                sendResponse(exchange, 401, "Invalid authentication key");
                return;
            }

            // Execute command
            if (json.has("command")) {
                String command = json.get("command").getAsString();
                LOGGER.info("Executing command from web: {}", command);

                boolean success = webServer.executeCommand(command);

                Map<String, Object> response = new HashMap<>();
                response.put("success", success);

                sendResponse(exchange, 200, GSON.toJson(response));
            } else {
                sendResponse(exchange, 400, "Missing command parameter");
            }

        } catch (Exception e) {
            LOGGER.error("Error processing request", e);
            sendResponse(exchange, 500, "Error processing request: " + e.getMessage());
        }
    }

    private boolean isIpAllowed(InetSocketAddress address) {
        if (webServer.getAllowedIps().isEmpty()) {
            return true;
        }

        String ip = address.getHostString();
        return webServer.getAllowedIps().contains(ip);
    }

    private void sendResponse(HttpExchange exchange, int code, String body) throws IOException {
        Headers headers = exchange.getResponseHeaders();
        headers.add("Content-Type", "application/json");

        byte[] responseBytes = body.getBytes(StandardCharsets.UTF_8);
        exchange.sendResponseHeaders(code, responseBytes.length);

        try (OutputStream os = exchange.getResponseBody()) {
            os.write(responseBytes);
        }
    }
}