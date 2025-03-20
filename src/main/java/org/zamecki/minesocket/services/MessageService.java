package org.zamecki.minesocket.services;

import net.minecraft.server.MinecraftServer;

import static org.zamecki.minesocket.ModData.logger;

public class MessageService {
    MinecraftServer server;

    public void setServer(MinecraftServer server) {
        this.server = server;
    }

    public void handleMessage(String message) {
        // Get the first word of the message
        String[] words = message.split(" ", 2);
        String command = words[0];
        String args = "";
        boolean hasArgs = words.length == 2;
        if (hasArgs) {
            args = words[1];
        }

        // Handle the command
        if (command.equalsIgnoreCase("command")) {
            if (!hasArgs) {
                logger.error("No command provided");
                return;
            }
            logger.info("Executing command: '{}'", args);
            server.getCommandManager().executeWithPrefix(server.getCommandSource(), args);
        }
    }
}
