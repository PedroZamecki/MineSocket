package org.zamecki.minesocket;

import net.fabricmc.api.ModInitializer;
import org.slf4j.Logger;
import org.zamecki.minesocket.controller.LangController;

public class MineSocket implements ModInitializer {
    String MOD_ID = "minesocket";
    Logger logger = org.slf4j.LoggerFactory.getLogger("MineSocket");
    LangController langController;

    @Override
    public void onInitialize() {
        logger.info("MineSocket is initializing");

        // Initialize the LangController
        langController = new LangController(MOD_ID, logger);
    }
}
