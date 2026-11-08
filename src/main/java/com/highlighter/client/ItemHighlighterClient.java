package com.highlighter.client;

import com.highlighter.ItemHighlighterMod;
import com.highlighter.config.ModConfig;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.TextColor;
import net.minecraft.resources.Identifier;
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
        ItemHighlighterMod.LOGGER.info("[Item Highlighter] Client initialized for Minecraft 26.3 with RenderPearl pedestal shelf.");
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

    public static int resolveColor(ItemStack stack) {
        if (stack == null || stack.isEmpty()) {
            return ModConfig.get().getDefaultColorRgb();
        }

        ModConfig config = ModConfig.get();
        String mode = config.colorMode != null ? config.colorMode.toUpperCase() : "DEFAULT";

        if ("DEFAULT".equals(mode) || "SINGLE_COLOR".equals(mode)) {
            return config.getDefaultColorRgb();
        }

        Identifier itemLoc = BuiltInRegistries.ITEM.getKey(stack.getItem());
        String itemId = itemLoc.toString();
        String namespace = itemLoc.getNamespace();

        if (config.itemColors != null && config.itemColors.containsKey(itemId)) {
            return config.parseHexColor(config.itemColors.get(itemId), config.getDefaultColorRgb());
        }

        if (config.modColors != null && config.modColors.containsKey(namespace)) {
            return config.parseHexColor(config.modColors.get(namespace), config.getDefaultColorRgb());
        }

        if ("ITEM_NAME".equals(mode)) {
            TextColor nameColor = stack.getHoverName().getStyle().getColor();
            if (nameColor != null) {
                return nameColor.getValue();
            }
        }

        if ("RARITY".equals(mode)) {
            Rarity rarity = stack.getRarity();
            String rarityKey = rarity.name().toUpperCase();
            if (config.rarityColors != null && config.rarityColors.containsKey(rarityKey)) {
                return config.parseHexColor(config.rarityColors.get(rarityKey), config.getDefaultColorRgb());
            }
        }

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

        if ("AUTO_ITEM".equals(mode)) {
            return generatePastelColor(itemId);
        }

        return config.getDefaultColorRgb();
    }

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

    public static void renderSlotHighlight(GuiGraphicsExtractor extractor, Slot slot) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player != null && mc.player.isCreative()) {
            return;
        }

        int containerSlot = slot.getContainerSlot();
        if (isMarked(containerSlot)) {
            if (slot.hasItem() && !slot.getItem().isEmpty()) {
                int color = resolveColor(slot.getItem());
                renderShelf(extractor, slot.x - 1, slot.y - 1, color);
            } else {
                clearMark(containerSlot);
            }
        }
    }

    public static void renderShelf(GuiGraphicsExtractor extractor, int x, int y, int colorRgb) {
        Identifier texture = ShelfTextureManager.getOrCreateShelfTexture(colorRgb);
        // Render 18x18 pedestal shelf behind item using RenderPearl pipeline in 26.3
        extractor.blit(texture, x, y, x + 18, y + 18, 0.0f, 1.0f, 0.0f, 1.0f);
    }
}
