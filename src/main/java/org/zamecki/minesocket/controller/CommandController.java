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
                .executes(this::sendHelp)
                .then(CommandManager.literal("help").executes(this::sendHelp))
                .then(CommandManager.literal("start").executes(ctx -> startWebSocket(ctx, wsService)))
                .then(CommandManager.literal("stop").executes(ctx -> stopWebSocket(ctx, wsService)))));
    }

    private int sendHelp(CommandContext<ServerCommandSource> ctx) {
        ctx.getSource().sendFeedback(() -> Text.translatableWithFallback(MOD_ID +
                ".command.help",
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
                ctx.getSource().sendFeedback(() -> Text.translatableWithFallback(MOD_ID +
                        ".command.start_error",
                    "An error occurred while starting the WebSocket server"), false);
                return 0;
            }
            ctx.getSource().sendFeedback(() -> Text.translatableWithFallback(MOD_ID +
                    ".command.started",
                "WebSocket server started"), false);
        } else {
            ctx.getSource().sendFeedback(() -> Text.translatableWithFallback(MOD_ID +
                    ".command.already_running",
                "WebSocket server is already running"), false);
        }
        return 1;
    }

    private int stopWebSocket(CommandContext<ServerCommandSource> ctx, WebSocketService wsService) {
        if (wsService.isRunning()) {
            if (!wsService.tryToStop()) {
                ctx.getSource().sendFeedback(() -> Text.translatableWithFallback(MOD_ID + ".command.stop_error",
                    "An error occurred while stopping the WebSocket server"), false);
                return 0;
            }
            ctx.getSource().sendFeedback(() -> Text.translatableWithFallback(MOD_ID + ".command.stopped",
                "WebSocket server stopped"), false);
        } else {
            ctx.getSource().sendFeedback(() -> Text.translatableWithFallback(MOD_ID + ".command.not_running",
                "WebSocket server is not running"), false);
        }
        return 1;
    }
}
