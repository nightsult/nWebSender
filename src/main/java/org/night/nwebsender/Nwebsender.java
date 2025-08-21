package org.night.nwebsender;

import com.mojang.logging.LogUtils;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.server.ServerStartingEvent;
import net.neoforged.neoforge.event.server.ServerStoppingEvent;
import org.night.nwebsender.commands.NWebsenderCommand;
import org.night.nwebsender.commands.NWebsenderTestCommands;
import org.night.nwebsender.config.NWebsenderConfig;
import org.night.nwebsender.events.EventListener;
import org.night.nwebsender.web.WebServer;
import org.slf4j.Logger;

import java.nio.file.Path;

@Mod(Nwebsender.MOD_ID)
public class Nwebsender {
    public static final String MOD_ID = "nwebsender";
    private static final Logger LOGGER = LogUtils.getLogger();

    private static WebServer webServer;
    public static NWebsenderConfig CONFIG;

    public Nwebsender(IEventBus modEventBus) {
        LOGGER.info("Initializing NWebsender");

        modEventBus.addListener(this::setup);

        NeoForge.EVENT_BUS.register(this);
        NeoForge.EVENT_BUS.register(EventListener.class);
    }

    private void setup(final net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent event) {
        LOGGER.info("NWebsender setup phase");
    }

    @SubscribeEvent
    public void onServerStarting(ServerStartingEvent event) {
        LOGGER.info("NWebsender server starting");

        Path configDir = event.getServer().getServerDirectory().resolve("config").resolve(MOD_ID);
        CONFIG = NWebsenderConfig.loadOrCreate(configDir.resolve("nwebsender-config.toml"));
        LOGGER.info("Configuration loaded, web server port: {}", CONFIG.port);

        NWebsenderCommand.register(event.getServer().getCommands().getDispatcher());
        NWebsenderTestCommands.register(event.getServer().getCommands().getDispatcher());

        try {
            startWebServer();
        } catch (Exception e) {
            LOGGER.error("Failed to start web server", e);
        }
    }

    @SubscribeEvent
    public void onServerStopping(ServerStoppingEvent event) {
        LOGGER.info("NWebsender server stopping");

        stopWebServer();
    }

    public static synchronized WebServer getWebServer() {
        return webServer;
    }

    public static synchronized boolean startWebServer() {
        if (webServer != null) {
            LOGGER.warn("Web server is already running");
            return false;
        }

        try {
            webServer = new WebServer(CONFIG.port, CONFIG.secretKey, CONFIG.allowedIps.split(","));
            webServer.start();
            LOGGER.info("Web server started on port {}", CONFIG.port);
            return true;
        } catch (Exception e) {
            LOGGER.error("Failed to start web server: {}", e.getMessage());
            return false;
        }
    }

    public static synchronized boolean stopWebServer() {
        if (webServer == null) {
            LOGGER.warn("Web server is not running");
            return false;
        }

        try {
            webServer.stop();
            webServer = null;
            LOGGER.info("Web server stopped");
            return true;
        } catch (Exception e) {
            LOGGER.error("Failed to stop web server: {}", e.getMessage());
            return false;
        }
    }

    public static synchronized boolean restartWebServer() {
        stopWebServer();
        return startWebServer();
    }
}