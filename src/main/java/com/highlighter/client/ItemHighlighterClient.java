package com.highlighter.client;

import com.highlighter.ItemHighlighterMod;
import com.highlighter.config.ModConfig;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.TextColor;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Environment(EnvType.CLIENT)
public class ItemHighlighterClient implements ClientModInitializer {
    private static final Set<Integer> MARKED_SLOTS = ConcurrentHashMap.newKeySet();

    @Override
    public void onInitializeClient() {
        ModConfig.load();
        ItemHighlighterMod.LOGGER.info("[Item Highlighter] Client initialized successfully with cute pastel 3D shelf renderer.");
    }

    public static void markSlot(int slotIndex) {
        if (slotIndex >= 0 && slotIndex < 36) {
            MARKED_SLOTS.add(slotIndex);
        }
    }

    public static void clearMark(int slotIndex) {
        MARKED_SLOTS.remove(slotIndex);
    }

    public static void clearAll() {
        MARKED_SLOTS.clear();
    }

    public static boolean isMarked(int slotIndex) {
        return MARKED_SLOTS.contains(slotIndex);
    }

    /**
     * Resolves the RGB color for the item based on config mode:
     * - "DEFAULT" / "SINGLE_COLOR" : Consistent warm buttercup yellow for all items.
     * - "RARITY"                   : Colors by item rarity (Yellow, Mint, Cyan, Lavender).
     * - "AUTO_MOD"                 : Auto-derives unique pastel colors per mod namespace.
     * - "MOD"                      : Explicit mod ID color mapping.
     * - "ITEM"                     : Direct item ID override.
     * - "ITEM_NAME"                : Custom name color.
     */
    public static int resolveColor(ItemStack stack) {
        if (stack == null || stack.isEmpty()) {
            return ModConfig.get().getDefaultColorRgb();
        }

        ModConfig config = ModConfig.get();
        String mode = config.colorMode != null ? config.colorMode.toUpperCase() : "DEFAULT";

        // 1. Single Color Mode (Default): All items use the exact same clean, warm buttercup yellow
        if ("DEFAULT".equals(mode) || "SINGLE_COLOR".equals(mode)) {
            return config.getDefaultColorRgb();
        }

        ResourceLocation itemLoc = BuiltInRegistries.ITEM.getKey(stack.getItem());
        String itemId = itemLoc.toString();
        String namespace = itemLoc.getNamespace();

        // 2. Direct item ID mapping override (e.g. "minecraft:diamond": "#67E8F9")
        if (config.itemColors != null && config.itemColors.containsKey(itemId)) {
            return config.parseHexColor(config.itemColors.get(itemId), config.getDefaultColorRgb());
        }

        // 3. Explicit mod ID / namespace color mapping (e.g. "create": "#FB923C")
        if (config.modColors != null && config.modColors.containsKey(namespace)) {
            return config.parseHexColor(config.modColors.get(namespace), config.getDefaultColorRgb());
        }

        // 4. Custom item name color (if dyed or enchanted)
        if ("ITEM_NAME".equals(mode)) {
            TextColor nameColor = stack.getHoverName().getStyle().getColor();
            if (nameColor != null) {
                return nameColor.getValue();
            }
        }

        // 5. Rarity mode
        if ("RARITY".equals(mode)) {
            Rarity rarity = stack.getRarity();
            String rarityKey = rarity.name().toUpperCase();
            if (config.rarityColors != null && config.rarityColors.containsKey(rarityKey)) {
                return config.parseHexColor(config.rarityColors.get(rarityKey), config.getDefaultColorRgb());
            }
        }

        // 6. AUTO_MOD mode: automatically gives each mod its own harmonious pastel color
        if ("AUTO_MOD".equals(mode)) {
            if ("minecraft".equals(namespace)) {
                Rarity rarity = stack.getRarity();
                if (rarity != Rarity.COMMON) {
                    String rarityKey = rarity.name().toUpperCase();
                    if (config.rarityColors != null && config.rarityColors.containsKey(rarityKey)) {
                        return config.parseHexColor(config.rarityColors.get(rarityKey), config.getDefaultColorRgb());
                    }
                }
                return config.getDefaultColorRgb();
            }
            return generatePastelColor(namespace);
        }

        // 7. AUTO_ITEM mode: distinct cute pastel colors per item
        if ("AUTO_ITEM".equals(mode)) {
            return generatePastelColor(itemId);
        }

        return config.getDefaultColorRgb();
    }

    /**
     * Mathematically generates a vibrant, cute pastel RGB color from any string seed (mod ID or item ID).
     */
    public static int generatePastelColor(String seed) {
        int hash = Math.abs(seed.hashCode());
        float hue = (hash % 360) / 360.0f;
        float saturation = 0.74f;
        float lightness = 0.72f;
        return hslToRgb(hue, saturation, lightness);
    }

    private static int hslToRgb(float h, float s, float l) {
        float q = l < 0.5f ? l * (1.0f + s) : l + s - l * s;
        float p = 2.0f * l - q;
        float r = hueToRgb(p, q, h + 1.0f / 3.0f);
        float g = hueToRgb(p, q, h);
        float b = hueToRgb(p, q, h - 1.0f / 3.0f);
        return ((Math.round(r * 255.0f) & 0xFF) << 16)
             | ((Math.round(g * 255.0f) & 0xFF) << 8)
             | (Math.round(b * 255.0f) & 0xFF);
    }

    private static float hueToRgb(float p, float q, float t) {
        if (t < 0.0f) t += 1.0f;
        if (t > 1.0f) t -= 1.0f;
        if (t < 1.0f / 6.0f) return p + (q - p) * 6.0f * t;
        if (t < 1.0f / 2.0f) return q;
        if (t < 2.0f / 3.0f) return p + (q - p) * (2.0f / 3.0f - t) * 6.0f;
        return p;
    }

    public static void onPreItemPickup(ItemStack item) {
        if (item == null || item.isEmpty()) {
            return;
        }

        Minecraft mc = Minecraft.getInstance();
        Player player = mc.player;
        if (player == null || player.isCreative()) {
            return;
        }

        int slot = player.getInventory().getSlotWithRemainingSpace(item);
        if (slot == -1) {
            slot = player.getInventory().getFreeSlot();
        }

        if (slot != -1) {
            markSlot(slot);
        }
    }

    public static void onSlotHovered(Slot slot) {
        if (ModConfig.get().clearOnHover && slot != null) {
            clearMark(slot.getContainerSlot());
        }
    }

    public static void onSlotClicked(Slot slot) {
        if (ModConfig.get().clearOnClick && slot != null) {
            clearMark(slot.getContainerSlot());
        }
    }

    public static void renderSlotHighlight(GuiGraphics graphics, Slot slot) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player != null && mc.player.isCreative()) {
            return;
        }

        int containerSlot = slot.getContainerSlot();
        if (isMarked(containerSlot)) {
            if (slot.hasItem() && !slot.getItem().isEmpty()) {
                int color = resolveColor(slot.getItem());
                renderShelf(graphics, slot.x - 1, slot.y - 1, color);
            } else {
                clearMark(containerSlot);
            }
        }
    }

    public static void renderHotbarHighlight(GuiGraphics graphics, int slotIndex, int x, int y, ItemStack item) {
        if (!ModConfig.get().showOnHotbar) {
            return;
        }

        Minecraft mc = Minecraft.getInstance();
        if (mc.player != null && mc.player.isCreative()) {
            return;
        }

        if (isMarked(slotIndex)) {
            if (item != null && !item.isEmpty()) {
                int color = resolveColor(item);
                renderShelf(graphics, x - 1, y - 1, color);
            } else {
                clearMark(slotIndex);
            }
        }
    }

    private static void renderShelf(GuiGraphics graphics, int x, int y, int colorRgb) {
        ResourceLocation texture = ShelfTextureManager.getOrCreateShelfTexture(colorRgb);

        ModConfig config = ModConfig.get();
        if (config.pulseEffect) {
            float pulse = (float) (Math.sin((Util.getMillis() % 2400L) / 2400.0 * 2.0 * Math.PI) * 0.05 + 0.95);
            graphics.setColor(pulse, pulse, pulse, 1.0f);
        }

        // Render the 18x18 cute pastel shelf behind the item
        graphics.blit(texture, x, y, 0.0f, 0.0f, 18, 18, 18, 18);

        if (config.pulseEffect) {
            graphics.setColor(1.0f, 1.0f, 1.0f, 1.0f);
        }
    }
}
