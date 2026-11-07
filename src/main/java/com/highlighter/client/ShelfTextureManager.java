package com.highlighter.client;

import com.highlighter.ItemHighlighterMod;
import com.highlighter.config.ModConfig;
import com.mojang.blaze3d.platform.NativeImage;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FastColor;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ShelfTextureManager {
    private static final Map<Integer, ResourceLocation> TEXTURE_CACHE = new ConcurrentHashMap<>();
    private static final int TEXTURE_SIZE = 18;

    /**
     * Retrieves or procedurally generates an 18x18 cute, vibrant pastel shelf texture.
     * Features:
     * - Prominent, crisp translucent border that outlines the slot clearly (especially with multiple adjacent items)
     * - Clean inset depth: soft inner shadow along the top/left edges, subtle light on the bottom/right lip
     * - Dreamy circular halo radiating outward behind the item
     * - Faint, subtle warm cream-white / pastel sparkle in the center
     * - Soft pastel grounding on the lower half (recessed cushion, vibrant and never muddy)
     */
    public static ResourceLocation getOrCreateShelfTexture(int baseRgb) {
        int rgbKey = baseRgb & 0x00FFFFFF;
        return TEXTURE_CACHE.computeIfAbsent(rgbKey, ShelfTextureManager::generateShelfTexture);
    }

    private static ResourceLocation generateShelfTexture(int rgbKey) {
        NativeImage image = new NativeImage(NativeImage.Format.RGBA, TEXTURE_SIZE, TEXTURE_SIZE, false);

        int baseR = (rgbKey >> 16) & 0xFF;
        int baseG = (rgbKey >> 8) & 0xFF;
        int baseB = rgbKey & 0xFF;

        // Dreamy cream-white sparkle accent for cute aesthetic
        int sparkleR = 255;
        int sparkleG = 248;
        int sparkleB = 236;

        ModConfig config = ModConfig.get();

        for (int y = 0; y < TEXTURE_SIZE; y++) {
            for (int x = 0; x < TEXTURE_SIZE; x++) {
                // 1. Outer Border: prominent, crisp translucent frame on the inventory plane
                if (x == 0 || x == TEXTURE_SIZE - 1 || y == 0 || y == TEXTURE_SIZE - 1) {
                    int r = Math.min(255, (int) (baseR * 1.06f));
                    int g = Math.min(255, (int) (baseG * 1.06f));
                    int b = Math.min(255, (int) (baseB * 1.06f));
                    int a = Math.clamp((int) (config.borderAlpha * 255.0f), 120, 245);
                    image.setPixelRGBA(x, y, FastColor.ABGR32.color(a, b, g, r));
                    continue;
                }

                // 2. Interior 16x16: ix: 0..15, iy: 0..15
                int ix = x - 1;
                int iy = y - 1;

                // Center coordinates [-1, 1]
                float cx = 7.5f;
                float cy = 7.5f;
                float dx = (ix - cx) / 7.5f;
                float dy = (iy - cy) / 7.5f;
                float dist = (float) Math.sqrt(dx * dx + dy * dy);

                // Round circular halo with smooth quadratic falloff
                float circleHalo = 0.0f;
                if (config.haloEffect) {
                    circleHalo = Math.max(0.0f, 1.0f - (dist / 1.05f) * (dist / 1.05f));
                }

                // Dreamy center sparkle / faint warm glow (turned down quite a lot)
                float sparkle = 0.0f;
                if (config.centerGoldAccent) {
                    sparkle = Math.max(0.0f, 1.0f - dist * 1.6f) * config.faintGlowIntensity;
                }

                // Clean inset shading: subtle top/left inner shadow, subtle bottom lip
                float insetFactor = 1.0f;
                if (config.insetShading) {
                    if (iy == 0) insetFactor *= 0.74f; // Soft top recess
                    if (ix == 0) insetFactor *= 0.78f; // Soft left recess
                    if (iy == 15 && ix > 0 && ix < 15) insetFactor *= 1.10f; // Soft bottom lip
                    if (ix == 15 && iy > 0 && iy < 15) insetFactor *= 1.06f; // Soft right lip
                }

                // Soft pastel grounding on lower half (> 50% height, iy >= 8) - kept vibrant and cute!
                float shelfGround = 0.0f;
                if (config.shelfDepth && iy >= 8) {
                    shelfGround = ((iy - 7) / 8.0f) * 0.12f;
                }

                // Color mixing with center sparkle
                float cr = baseR * (1.0f - sparkle) + sparkleR * sparkle;
                float cg = baseG * (1.0f - sparkle) + sparkleG * sparkle;
                float cb = baseB * (1.0f - sparkle) + sparkleB * sparkle;

                // Brightness: bright, vibrant, and pastel
                float brightness = (0.92f + circleHalo * 0.22f - shelfGround) * insetFactor;
                brightness = Math.clamp(brightness, 0.45f, 1.25f);

                int finalR = Math.clamp((int) (cr * brightness), 0, 255);
                int finalG = Math.clamp((int) (cg * brightness), 0, 255);
                int finalB = Math.clamp((int) (cb * brightness), 0, 255);

                // Translucent watercolor wash alpha
                float innerAlpha = 0.50f + circleHalo * 0.20f + (iy >= 8 ? 0.08f : 0.0f);
                int a = Math.clamp((int) (innerAlpha * 255.0f), 80, 205);

                image.setPixelRGBA(x, y, FastColor.ABGR32.color(a, finalB, finalG, finalR));
            }
        }

        DynamicTexture texture = new DynamicTexture(image);
        String name = "dynamic_cute_shelf_" + Integer.toHexString(rgbKey);
        ResourceLocation location = ResourceLocation.fromNamespaceAndPath(ItemHighlighterMod.MOD_ID, name);
        Minecraft.getInstance().getTextureManager().register(location, texture);
        return location;
    }

    public static void clearCache() {
        TEXTURE_CACHE.clear();
    }
}
