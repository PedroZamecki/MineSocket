package org.zamecki.minesocket;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.text.Text;
import org.slf4j.Logger;
import org.zamecki.minesocket.controller.CommandController;
import org.zamecki.minesocket.controller.LangController;
import org.zamecki.minesocket.controller.SettingsController;
import org.zamecki.minesocket.services.MessageService;
import org.zamecki.minesocket.services.WebSocketService;

import java.net.InetSocketAddress;

public class MineSocket implements ModInitializer {
    String MOD_ID = "minesocket";
    Logger logger = org.slf4j.LoggerFactory.getLogger("MineSocket");
    LangController langController;
    SettingsController settingsController;
    WebSocketService wsService;
    MessageService messageService;

    @Override
    public void onInitialize() {
        logger.info("MineSocket is initializing");

        // Initialize the LangController
        langController = new LangController(MOD_ID, logger);

        // Initialize the SettingsController
        settingsController = new SettingsController(MOD_ID, logger);

        // Register events callbacks
        registerEventsCallbacks();

        // Initialize the WebSocketService
        String host = "localhost";
        int port = 8887;
        messageService = new MessageService(logger);
        wsService = new WebSocketService(new InetSocketAddress(host, port), logger, langController, messageService);

        // Register the commands
        CommandController.register(wsService);
    }


    private void registerEventsCallbacks() {
        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
            var player = handler.getPlayer();
            if (player.getPermissionLevel() >= server.getOpPermissionLevel()) {
                player.sendMessage(Text.translatableWithFallback(MOD_ID + ".callback.on_op_join", "You are using MineSocket, you can configure/use the mod by using the '/ms' command "));
            }
        });

        ServerLifecycleEvents.SERVER_STARTED.register(server -> {
            messageService.setServer(server);
            boolean res = wsService.tryToStart();
            if (!res) {
                server.sendMessage(Text.translatableWithFallback(MOD_ID + ".callback.on_open_error", "MineSocket WebSocket server failed to start"));
                return;
            }
            server.sendMessage(Text.translatableWithFallback(MOD_ID + ".callback.on_open", "MineSocket WebSocket server started"));
        });

        ServerLifecycleEvents.SERVER_STOPPING.register(server -> {
            boolean res = wsService.tryToStop();
            if (!res) {
                server.sendMessage(Text.translatableWithFallback(MOD_ID + ".callback.on_close_error", "MineSocket WebSocket server failed to stop"));
                return;
            }
            server.sendMessage(Text.translatableWithFallback(MOD_ID + ".callback.on_close", "MineSocket WebSocket server stopped"));
        });
    }
}
