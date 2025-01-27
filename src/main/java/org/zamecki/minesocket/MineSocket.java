package org.zamecki.minesocket;

import net.fabricmc.api.ModInitializer;
import org.slf4j.Logger;

public class MineSocket implements ModInitializer {
    Logger logger = org.slf4j.LoggerFactory.getLogger("MineSocket");
    
    @Override
    public void onInitialize() {
        logger.info("MineSocket is initializing");
    }
}
