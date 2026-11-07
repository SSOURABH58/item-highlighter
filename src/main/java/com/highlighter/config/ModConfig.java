package com.highlighter.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.highlighter.ItemHighlighterMod;
import net.fabricmc.loader.api.FabricLoader;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;

public class ModConfig {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final File CONFIG_FILE = new File(FabricLoader.getInstance().getConfigDir().toFile(), "item_highlighter.json");
    private static ModConfig INSTANCE = new ModConfig();

    /**
     * Color Mode Options:
     * - "DEFAULT" / "SINGLE_COLOR" : (Default) Highlights every newly picked up item in the same clean,
     *                                neutral warm pastel yellow (#F6D365).
     * - "RARITY"                   : Colors by item rarity using rarityColors below (Common=Yellow, Uncommon=Mint, Rare=Cyan, Epic=Lavender).
     * - "AUTO_MOD"                 : Automatically gives each mod/namespace its own unique cute pastel color signature.
     * - "MOD"                      : Uses explicit colors from the modColors mapping below.
     * - "ITEM"                     : Colors specific items from itemColors, falling back to default.
     * - "ITEM_NAME"                : Matches the formatted/dyed tooltip name color of the item.
     */
    public String colorMode = "RARITY";

    // Neutral, warm cute pastel yellow for highlighted items (#F6D365)
    public String defaultColor = "#F6D365";

    // Custom colors mapped by Mod ID / namespace (e.g. "minecraft", "create", "farmersdelight")
    public Map<String, String> modColors = new LinkedHashMap<>();

    // Custom colors mapped by Item Rarity (COMMON, UNCOMMON, RARE, EPIC)
    public Map<String, String> rarityColors = new LinkedHashMap<>();

    // Custom colors mapped by exact Item ID (e.g. "minecraft:diamond")
    public Map<String, String> itemColors = new LinkedHashMap<>();

    // Border styling
    public float borderAlpha = 0.85f;        // Prominent translucent border (approx 85% opacity)

    // Visual features
    public boolean insetShading = true;      // Soft inner shadow on top/left for clean recessed depth
    public boolean shelfDepth = true;        // Gentle pastel grounding on the lower half
    public boolean haloEffect = true;        // Circular halo glow behind the item
    public boolean centerGoldAccent = true;  // Dreamy center sparkle/glow
    public float faintGlowIntensity = 0.20f; // Faint glow intensity (subtle 20% dreaminess)
    public boolean pulseEffect = true;       // Very subtle breathing pulse

    // Interactive clearing options
    public boolean clearOnHover = true;           // Clears highlight when hovered in inventory
    public boolean clearOnClick = true;           // Clears highlight when clicked or moved
    public boolean clearOnSelect = true;          // Clears highlight when hotbar slot is selected
    public boolean clearOnInventoryClose = false; // Clears all highlights when closing inventory
    public boolean showOnHotbar = true;           // Shows shelf highlight on the in-game HUD hotbar

    public ModConfig() {
        // Sample mappings ready for users who want to switch to "MOD" or "RARITY" modes
        modColors.put("minecraft", "#F6D365");      // Neutral Warm Buttercup Yellow
        modColors.put("create", "#FB923C");         // Pastel Tangerine
        modColors.put("farmersdelight", "#86EFAC"); // Mint Green
        modColors.put("botania", "#7DD3FC");        // Pastel Sky Cyan
        modColors.put("jei", "#C084FC");            // Pastel Lavender

        rarityColors.put("COMMON", "#F6D365");      // Buttercup Yellow
        rarityColors.put("UNCOMMON", "#86EFAC");    // Mint Green
        rarityColors.put("RARE", "#7DD3FC");        // Pastel Cyan
        rarityColors.put("EPIC", "#C084FC");        // Lavender Dream

        itemColors.put("minecraft:diamond", "#67E8F9");
        itemColors.put("minecraft:golden_apple", "#FCD34D");
        itemColors.put("minecraft:netherite_ingot", "#E879F9");
        itemColors.put("minecraft:totem_of_undying", "#FACC15");
    }

    public static ModConfig get() {
        return INSTANCE;
    }

    public static void load() {
        if (CONFIG_FILE.exists()) {
            try (FileReader reader = new FileReader(CONFIG_FILE)) {
                INSTANCE = GSON.fromJson(reader, ModConfig.class);
                if (INSTANCE == null) {
                    INSTANCE = new ModConfig();
                }
            } catch (Exception e) {
                ItemHighlighterMod.LOGGER.error("Failed to load Item Highlighter config, using defaults.", e);
                INSTANCE = new ModConfig();
            }
        } else {
            save();
        }
    }

    public static void save() {
        try {
            CONFIG_FILE.getParentFile().mkdirs();
            try (FileWriter writer = new FileWriter(CONFIG_FILE)) {
                GSON.toJson(INSTANCE, writer);
            }
        } catch (IOException e) {
            ItemHighlighterMod.LOGGER.error("Failed to save Item Highlighter config.", e);
        }
    }

    public int parseHexColor(String hex, int fallback) {
        if (hex == null || hex.isEmpty()) {
            return fallback;
        }
        try {
            String clean = hex.trim();
            if (clean.startsWith("#")) clean = clean.substring(1);
            if (clean.startsWith("0x") || clean.startsWith("0X")) clean = clean.substring(2);
            return (int) Long.parseLong(clean, 16) & 0x00FFFFFF;
        } catch (Exception e) {
            return fallback;
        }
    }

    public int getDefaultColorRgb() {
        return parseHexColor(defaultColor, 0xF6D365);
    }
}
