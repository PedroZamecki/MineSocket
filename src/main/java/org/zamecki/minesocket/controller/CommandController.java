package org.zamecki.minesocket.controller;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;

public class CommandController {
    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(CommandManager.literal("ms")
            .then(CommandManager.argument("message", StringArgumentType.string())
                .executes(context -> {
                    String message = StringArgumentType.getString(context, "message");
                    context.getSource().sendFeedback(() -> Text.of("Received message: " + message), false);
                    return 1;
                }))
            .executes(context -> {
                context.getSource().sendFeedback(() -> Text.literal("Usage: /ms <message>"), false);
                return 1;
            }));
    }
}
