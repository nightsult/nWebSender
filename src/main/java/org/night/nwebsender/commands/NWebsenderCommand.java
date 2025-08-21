package org.night.nwebsender.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import org.night.nwebsender.Nwebsender;
import org.night.nwebsender.web.WebServer;

public class NWebsenderCommand {
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("nwebsender")
                .requires(source -> source.hasPermission(4)) // OP level 4
                .then(Commands.literal("restart")
                        .executes(NWebsenderCommand::restartWebServer))
                .then(Commands.literal("stop")
                        .executes(NWebsenderCommand::stopWebServer))
                .then(Commands.literal("start")
                        .executes(NWebsenderCommand::startWebServer))
                .then(Commands.literal("status")
                        .executes(NWebsenderCommand::showStatus))
        );
    }

    private static int restartWebServer(CommandContext<CommandSourceStack> ctx) {
        ctx.getSource().sendSuccess(() -> Component.literal("Restarting NWebsender web server..."), true);

        if (Nwebsender.restartWebServer()) {
            ctx.getSource().sendSuccess(() -> Component.literal("Web server restarted successfully on port " +
                    Nwebsender.CONFIG.port), true);
            return 1;
        } else {
            ctx.getSource().sendFailure(Component.literal("Failed to restart web server"));
            return 0;
        }
    }

    private static int stopWebServer(CommandContext<CommandSourceStack> ctx) {
        if (Nwebsender.stopWebServer()) {
            ctx.getSource().sendSuccess(() -> Component.literal("Web server stopped"), true);
            return 1;
        } else {
            ctx.getSource().sendFailure(Component.literal("Web server is not running or couldn't be stopped"));
            return 0;
        }
    }

    private static int startWebServer(CommandContext<CommandSourceStack> ctx) {
        if (Nwebsender.startWebServer()) {
            ctx.getSource().sendSuccess(() -> Component.literal("Web server started on port " +
                    Nwebsender.CONFIG.port), true);
            return 1;
        } else {
            ctx.getSource().sendFailure(Component.literal("Failed to start web server or it's already running"));
            return 0;
        }
    }

    private static int showStatus(CommandContext<CommandSourceStack> ctx) {
        WebServer webServer = Nwebsender.getWebServer();
        if (webServer != null) {
            ctx.getSource().sendSuccess(() -> Component.literal("Web server is running on port " +
                    Nwebsender.CONFIG.port), false);
            ctx.getSource().sendSuccess(() -> Component.literal("Allowed IPs: " +
                    Nwebsender.CONFIG.allowedIps), false);
        } else {
            ctx.getSource().sendSuccess(() -> Component.literal("Web server is not running"), false);
        }
        return 1;
    }
}