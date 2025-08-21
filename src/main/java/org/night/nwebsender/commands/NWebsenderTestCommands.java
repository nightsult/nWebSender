package org.night.nwebsender.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

public class NWebsenderTestCommands {
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("nwtest")
                .requires(source -> source.hasPermission(2)) // OP level 2
                .then(Commands.literal("message")
                        .executes(NWebsenderTestCommands::broadcastMessage))
                .then(Commands.literal("heal")
                        .executes(NWebsenderTestCommands::healPlayers))
                .then(Commands.literal("time")
                        .then(Commands.literal("day")
                                .executes(NWebsenderTestCommands::setDay))
                        .then(Commands.literal("night")
                                .executes(NWebsenderTestCommands::setNight)))
                .then(Commands.literal("weather")
                        .then(Commands.literal("clear")
                                .executes(NWebsenderTestCommands::setClear))
                        .then(Commands.literal("rain")
                                .executes(NWebsenderTestCommands::setRain))
                        .then(Commands.literal("thunder")
                                .executes(NWebsenderTestCommands::setThunder)))
        );
    }

    private static int broadcastMessage(CommandContext<CommandSourceStack> ctx) {
        ctx.getSource().getServer().getPlayerList().broadcastSystemMessage(
                Component.literal("§aTest message from NWebsender!"), false);
        return 1;
    }

    private static int healPlayers(CommandContext<CommandSourceStack> ctx) {
        int count = 0;
        for (ServerPlayer player : ctx.getSource().getServer().getPlayerList().getPlayers()) {
            player.setHealth(player.getMaxHealth());
            player.getFoodData().setFoodLevel(20);
            player.displayClientMessage(Component.literal("§aYou have been healed!"), true);
            count++;
        }

        ctx.getSource().sendSuccess(() -> Component.literal("Healed players"), true);
        return count;
    }

    private static int setDay(CommandContext<CommandSourceStack> ctx) {
        ctx.getSource().getLevel().setDayTime(1000);
        ctx.getSource().sendSuccess(() -> Component.literal("Time set to day"), true);
        return 1;
    }

    private static int setNight(CommandContext<CommandSourceStack> ctx) {
        ctx.getSource().getLevel().setDayTime(13000);
        ctx.getSource().sendSuccess(() -> Component.literal("Time set to night"), true);
        return 1;
    }

    private static int setClear(CommandContext<CommandSourceStack> ctx) {
        ctx.getSource().getLevel().setWeatherParameters(6000, 0, false, false);
        ctx.getSource().sendSuccess(() -> Component.literal("Weather set to clear"), true);
        return 1;
    }

    private static int setRain(CommandContext<CommandSourceStack> ctx) {
        ctx.getSource().getLevel().setWeatherParameters(0, 6000, true, false);
        ctx.getSource().sendSuccess(() -> Component.literal("Weather set to rain"), true);
        return 1;
    }

    private static int setThunder(CommandContext<CommandSourceStack> ctx) {
        ctx.getSource().getLevel().setWeatherParameters(0, 6000, true, true);
        ctx.getSource().sendSuccess(() -> Component.literal("Weather set to thunder"), true);
        return 1;
    }
}