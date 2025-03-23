package org.zamecki.minesocket.controller;

import com.mojang.brigadier.context.CommandContext;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;
import org.zamecki.minesocket.services.WebSocketService;

import static org.zamecki.minesocket.ModData.MOD_ID;

public class CommandController {

    public CommandController(WebSocketService wsService) {
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) ->
            dispatcher.register(CommandManager.literal("ms")
                // Uses the LuckPerms permission system to check if the player has the permission to use the command
                .requires(source -> source.hasPermissionLevel(source.getServer().getOpPermissionLevel()))
                .executes(this::sendHelp)
                .then(CommandManager.literal("help").executes(this::sendHelp))
                .then(CommandManager.literal("start").executes(ctx -> startWebSocket(ctx, wsService)))
                .then(CommandManager.literal("stop").executes(ctx -> stopWebSocket(ctx, wsService)))));
    }

    private int sendHelp(CommandContext<ServerCommandSource> ctx) {
        ctx.getSource().sendFeedback(() -> Text.translatableWithFallback(
            "command." + MOD_ID + ".help",
            """
                MineSocket Help:
                /ms - Main command
                /ms help - Show this help message
                /ms start - Start the WebSocket server
                /ms stop - Stop the WebSocket server"""), false);
        return 1;
    }

    private int startWebSocket(CommandContext<ServerCommandSource> ctx, WebSocketService wsService) {
        if (!wsService.isRunning()) {
            if (!wsService.tryToStart()) {
                ctx.getSource().sendFeedback(() -> Text.translatableWithFallback(
                    "command." + MOD_ID + ".start_error",
                    "An error occurred while starting the WebSocket server"), false);
                return 0;
            }
            ctx.getSource().sendFeedback(() -> Text.translatableWithFallback(
                "command." + MOD_ID + ".started",
                "WebSocket server started"), false);
        } else {
            ctx.getSource().sendFeedback(() -> Text.translatableWithFallback(
                "command." + MOD_ID + ".already_running",
                "WebSocket server is already running"), false);
        }
        return 1;
    }

    private int stopWebSocket(CommandContext<ServerCommandSource> ctx, WebSocketService wsService) {
        if (wsService.isRunning()) {
            if (!wsService.tryToStop()) {
                ctx.getSource().sendFeedback(() -> Text.translatableWithFallback("command." + MOD_ID + ".stop_error",
                    "An error occurred while stopping the WebSocket server"), false);
                return 0;
            }
            ctx.getSource().sendFeedback(() -> Text.translatableWithFallback("command." + MOD_ID + ".stopped",
                "WebSocket server stopped"), false);
        } else {
            ctx.getSource().sendFeedback(() -> Text.translatableWithFallback("command." + MOD_ID + ".not_running",
                "WebSocket server is not running"), false);
        }
        return 1;
    }
}
