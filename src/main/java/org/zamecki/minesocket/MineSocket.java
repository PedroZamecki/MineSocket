package org.zamecki.minesocket;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.text.Text;
import org.slf4j.Logger;
import org.zamecki.minesocket.controller.LangController;
import org.zamecki.minesocket.services.WebSocketService;

import java.net.InetSocketAddress;

public class MineSocket implements ModInitializer {
    String MOD_ID = "minesocket";
    Logger logger = org.slf4j.LoggerFactory.getLogger("MineSocket");
    LangController langController;
    WebSocketService wsService;

    @Override
    public void onInitialize() {
        logger.info("MineSocket is initializing");

        // Initialize the LangController
        langController = new LangController(MOD_ID, logger);

        // Register events callbacks
        registerEventsCallbacks();

        // Initialize the WebSocketService
        String host = "localhost";
        int port = 8887;
        wsService = new WebSocketService(new InetSocketAddress(host, port), logger, langController);
        wsService.start();
    }

    private void registerEventsCallbacks() {
        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
            var player = handler.getPlayer();
            if (player.getPermissionLevel() >= server.getOpPermissionLevel()) {
                player.sendMessage(Text.translatableWithFallback(MOD_ID + ".callback.on_op_join", "You are using MineSocket, you can configure/use the mod by using the '/ms' command "));
            }
        });
    }
}
