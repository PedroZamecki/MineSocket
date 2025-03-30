package org.zamecki.minesocket.services;

import net.minecraft.server.MinecraftServer;
import org.zamecki.minesocket.event.EventManager;

import static org.zamecki.minesocket.ModData.logger;

public class MessageService {
    MinecraftServer server;
    EventManager eventManager;

    public void setServer(MinecraftServer server) {
        this.server = server;
        eventManager = new EventManager(server);
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

        // Handle the event
        if (command.equalsIgnoreCase("event")) {
            String[] eventArgs = args.split(" ");
            String eventName = eventArgs[0];
            String[] eventParams = new String[eventArgs.length - 1];
            System.arraycopy(eventArgs, 1, eventParams, 0, eventArgs.length - 1);
            if (!eventManager.handleEvent(eventName, eventParams)) {
                logger.error("Event '{}' not found", eventName);
            }
        } else {
            logger.error("Unknown command or event: '{}'", command);
        }
    }

    public void tick() {
        eventManager.onServerTick();
    }
}

