package com.highlighter.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.highlighter.ItemHighlighterMod;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;

public class ModConfig {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final File CONFIG_FILE = new File("config", "item_highlighter.json");
    private static ModConfig INSTANCE = new ModConfig();

    public String colorMode = "DEFAULT";
    public String defaultColor = "#F6D365";
    public boolean haloEffect = true;
    public boolean centerGoldAccent = true;
    public float faintGlowIntensity = 0.28f;
    public boolean insetShading = true;
    public boolean shelfDepth = true;
    public boolean pulseEffect = true;
    public float borderAlpha = 0.82f;
    public boolean clearOnHover = true;
    public boolean clearOnClick = true;
    public boolean clearOnSelect = true;
    public boolean clearOnInventoryClose = false;
    public boolean showOnHotbar = true;

    public Map<String, String> rarityColors = new LinkedHashMap<>();
    public Map<String, String> modColors = new LinkedHashMap<>();
    public Map<String, String> itemColors = new LinkedHashMap<>();

    public ModConfig() {
        rarityColors.put("COMMON", "#F6D365");
        rarityColors.put("UNCOMMON", "#86EFAC");
        rarityColors.put("RARE", "#7DD3FC");
        rarityColors.put("EPIC", "#D8B4FE");

        modColors.put("minecraft", "#F6D365");
        modColors.put("create", "#FB923C");
        modColors.put("farmersdelight", "#4ADE80");
        modColors.put("botania", "#60A5FA");
        modColors.put("appliedenergistics2", "#A78BFA");

        itemColors.put("minecraft:netherite_ingot", "#F472B6");
        itemColors.put("minecraft:diamond", "#38BDF8");
    }

    public static ModConfig get() {
        return INSTANCE;
    }

    public static void load() {
        if (!CONFIG_FILE.exists()) {
            save();
            return;
        }

        try (FileReader reader = new FileReader(CONFIG_FILE)) {
            ModConfig loaded = GSON.fromJson(reader, ModConfig.class);
            if (loaded != null) {
                INSTANCE = loaded;
            }
        } catch (IOException e) {
            ItemHighlighterMod.LOGGER.error("[Item Highlighter] Failed to load config, using defaults.", e);
        }
    }

    public static void save() {
        File parent = CONFIG_FILE.getParentFile();
        if (parent != null && !parent.exists()) {
            parent.mkdirs();
        }

        try (FileWriter writer = new FileWriter(CONFIG_FILE)) {
            GSON.toJson(INSTANCE, writer);
        } catch (IOException e) {
            ItemHighlighterMod.LOGGER.error("[Item Highlighter] Failed to save config.", e);
        }
    }

    public int getDefaultColorRgb() {
        return parseHexColor(defaultColor, 0xF6D365);
    }

    public int parseHexColor(String hex, int fallback) {
        if (hex == null || hex.isEmpty()) {
            return fallback;
        }
        try {
            String clean = hex.startsWith("#") ? hex.substring(1) : hex;
            return Integer.parseInt(clean, 16);
        } catch (NumberFormatException e) {
            return fallback;
        }
    }
}
