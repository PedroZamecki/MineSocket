package org.zamecki.minesocket.controller;

import com.mojang.brigadier.context.CommandContext;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;
import org.zamecki.minesocket.services.WebSocketService;

public class CommandController {
    public static void register(WebSocketService wsService) {
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) ->
            dispatcher.register(CommandManager.literal("ms")
                .executes(CommandController::sendHelp)
                .then(CommandManager.literal("help").executes(CommandController::sendHelp))
                .then(CommandManager.literal("start").executes(ctx -> startWebSocket(ctx, wsService)))
                .then(CommandManager.literal("stop").executes(ctx -> stopWebSocket(ctx, wsService)))
            )
        );
    }

    private static int sendHelp(CommandContext<ServerCommandSource> ctx) {
        ctx.getSource().sendFeedback(() -> Text.literal(
            """
                MineSocket Help:
                /ms - Main command
                /ms help - Show this help message
                /ms start - Start the WebSocket server
                /ms stop - Stop the WebSocket server"""), false);
        return 1;
    }

    private static int startWebSocket(CommandContext<ServerCommandSource> ctx, WebSocketService wsService) {
        if (!wsService.isRunning()) {
            if (!wsService.tryToStart()) {
                ctx.getSource().sendFeedback(() -> Text.literal("Error starting WebSocket server"), false);
                return 0;
            }
            ctx.getSource().sendFeedback(() -> Text.literal("WebSocket server started"), false);
        } else {
            ctx.getSource().sendFeedback(() -> Text.literal("WebSocket server is already running"), false);
        }
        return 1;
    }

    private static int stopWebSocket(CommandContext<ServerCommandSource> ctx, WebSocketService wsService) {
        if (wsService.isRunning()) {
            if (!wsService.tryToStop()) {
                ctx.getSource().sendFeedback(() -> Text.literal("Error stopping WebSocket server"), false);
                return 0;
            }
            ctx.getSource().sendFeedback(() -> Text.literal("WebSocket server stopped"), false);
        } else {
            ctx.getSource().sendFeedback(() -> Text.literal("WebSocket server is not running"), false);
        }
        return 1;
    }
}
