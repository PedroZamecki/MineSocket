package org.zamecki.minesocket.services;

import net.minecraft.server.MinecraftServer;

public class MessageService {
    MinecraftServer server;

    public void setServer(MinecraftServer server) {
        this.server = server;
    }

    public void handleMessage(String message) {
        // Get the first word of the message
        String[] words = message.split(" ", 2);
        String command = words[0];
        String args = words[1];

        // Handle the command
        if (command.equalsIgnoreCase("command")) {
            server.getCommandManager().executeWithPrefix(server.getCommandSource(), args);
        }
    }
}
