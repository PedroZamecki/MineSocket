package org.zamecki.minesocket;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.text.Text;
import org.zamecki.minesocket.controller.CommandController;
import org.zamecki.minesocket.controller.SettingsController;
import org.zamecki.minesocket.services.MessageService;
import org.zamecki.minesocket.services.WebSocketService;

import java.net.InetSocketAddress;

import static org.zamecki.minesocket.ModData.MOD_ID;
import static org.zamecki.minesocket.ModData.logger;

public class MineSocket implements ModInitializer {
    SettingsController settingsController;
    WebSocketService wsService;
    MessageService messageService;
    CommandController commandController;

    @Override
    public void onInitialize() {
        logger.info("MineSocket is initializing");

        // Initialize the SettingsController
        settingsController = new SettingsController();

        // Register events callbacks
        registerEventsCallbacks();

        // Initialize the WebSocketService
        String host = "localhost";
        int port = 8887;
        messageService = new MessageService();
        wsService = new WebSocketService(new InetSocketAddress(host, port), messageService);

        // Register the commands
        commandController = new CommandController(wsService);
    }

    private void registerEventsCallbacks() {
        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
            var player = handler.getPlayer();
            if (player.getPermissionLevel() >= server.getOpPermissionLevel() && server.isDedicated()) {
                player.sendMessage(Text.translatableWithFallback(MOD_ID +
                        ".callback.on_op_join",
                    "You are using MineSocket, you can configure/use the mod by using the '/minesocket' command "));
            }
        });

        ServerLifecycleEvents.SERVER_STARTED.register(server -> {
            messageService.setServer(server);
            if (!server.isDedicated()) {
                server.sendMessage(Text.translatableWithFallback(MOD_ID +
                        ".callback.on_singleplayer",
                    "MineSocket is available in singleplayer, but you need to activate with the command '/minesocket'"));
                return;
            }

            boolean res = wsService.tryToStart();
            if (!res) {
                server.sendMessage(Text.translatableWithFallback(MOD_ID +
                        ".callback.on_open_error",
                    "MineSocket WebSocket server failed to start"));
                return;
            }

            server.sendMessage(Text.translatableWithFallback(MOD_ID +
                    ".callback.on_open",
                "MineSocket WebSocket server started"));
        });

        ServerLifecycleEvents.SERVER_STOPPING.register(server -> {
            boolean res = wsService.tryToStop();
            if (!res) {
                server.sendMessage(Text.translatableWithFallback(MOD_ID +
                        ".callback.on_close_error",
                    "MineSocket WebSocket server failed to stop"));
                return;
            }
            server.sendMessage(Text.translatableWithFallback(MOD_ID +
                    ".callback.on_close",
                "MineSocket WebSocket server stopped"));
        });
    }
}
