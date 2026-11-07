package com.highlighter;

import net.fabricmc.api.ModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ItemHighlighterMod implements ModInitializer {
    public static final String MOD_ID = "item_highlighter";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    @Override
    public void onInitialize() {
        LOGGER.info("[Item Highlighter] Initialized. Environment: Universal (Client/Server compatible).");
    }
}
