package org.night.nwebsender.events;

import com.mojang.logging.LogUtils;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.server.ServerStartedEvent;
import net.neoforged.neoforge.event.server.ServerStoppedEvent;
import org.slf4j.Logger;

public class EventListener {
    private static final Logger LOGGER = LogUtils.getLogger();

    @SubscribeEvent
    public static void onPlayerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
        LOGGER.info("Player {} logged in. This event could be used to notify a website.",
                event.getEntity().getGameProfile().getName());
    }

    @SubscribeEvent
    public static void onPlayerLoggedOut(PlayerEvent.PlayerLoggedOutEvent event) {
        LOGGER.info("Player {} logged out. This event could be used to notify a website.",
                event.getEntity().getGameProfile().getName());
    }

    @SubscribeEvent
    public static void onServerStarted(ServerStartedEvent event) {
        LOGGER.info("Server fully started. NWebsender is ready to receive commands.");
    }

    @SubscribeEvent
    public static void onServerStopped(ServerStoppedEvent event) {
        LOGGER.info("Server fully stopped.");
    }
}