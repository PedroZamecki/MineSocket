package org.zamecki.minesocket;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.text.Text;
import org.zamecki.minesocket.config.MineSocketConfiguration;
import org.zamecki.minesocket.controller.CommandController;
import org.zamecki.minesocket.services.MessageService;
import org.zamecki.minesocket.services.WebSocketService;

import static org.zamecki.minesocket.ModData.MOD_ID;
import static org.zamecki.minesocket.ModData.logger;

public class MineSocket implements ModInitializer {
    MineSocketConfiguration config;
    WebSocketService wsService;
    MessageService messageService;
    CommandController commandController;

    @Override
    public void onInitialize() {
        logger.info("MineSocket is initializing");

        // Initialize the configuration
        config = new MineSocketConfiguration();

        // Register events callbacks
        registerEventsCallbacks();

        // Initialize the WebSocketService
        messageService = new MessageService();
        wsService = new WebSocketService(config, messageService);

        // Register the commands
        commandController = new CommandController(wsService);
    }

    private void registerEventsCallbacks() {
        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
            var player = handler.getPlayer();

            if (!player.hasPermissionLevel(server.getOpPermissionLevel())) {
                return;
            }

            if (!server.isDedicated()) {
                server.sendMessage(Text.translatable("callback." + MOD_ID + ".on_singleplayer",
                    "MineSocket is available in singleplayer, but you need to activate with the command '/ms'"));
            }

            player.sendMessage(Text.translatable("callback." + MOD_ID + ".on_op_join",
                "You are using MineSocket, you can configure/use the mod by using the '/ms' command"));
        });

        ServerLifecycleEvents.SERVER_STARTED.register(server -> {
            messageService.setServer(server);
            if (!server.isDedicated() || !config.autoStart) {
                return;
            }

            boolean res = wsService.tryToStart();
            if (!res) {
                server.sendMessage(Text.translatable("callback." + MOD_ID + ".on_open_error",
                    "MineSocket WebSocket server failed to start"));
                return;
            }

            server.sendMessage(Text.translatable("callback." + MOD_ID + ".on_open",
                "MineSocket WebSocket server started"));
        });

        ServerLifecycleEvents.SERVER_STOPPING.register(server -> {
            if (!wsService.isRunning()) {
                return;
            }

            boolean res = wsService.tryToStop();
            if (!res) {
                server.sendMessage(Text.translatable("callback." + MOD_ID + ".on_close_error",
                    "MineSocket WebSocket server failed to stop"));
                return;
            }
            server.sendMessage(Text.translatable("callback." + MOD_ID + ".on_close",
                "MineSocket WebSocket server stopped"));
        });

        ServerLifecycleEvents.END_DATA_PACK_RELOAD.register(((server, resourceManager, success) -> {
            logger.info("Reloading configuration");
            try {
                config.reload();
                if (!wsService.tryToReload()) {
                    logger.error("Error reloading WebSocket server");
                }
            } catch(Exception e) {
                logger.error("Error reloading configuration: {}", e.getMessage());
            }
        }));
    }
}
