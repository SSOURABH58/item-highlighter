package com.highlighter.client;

import com.highlighter.ItemHighlighterMod;
import com.highlighter.config.ModConfig;
import com.mojang.blaze3d.platform.NativeImage;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.resources.Identifier;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ShelfTextureManager {
    private static final Map<Integer, Identifier> TEXTURE_CACHE = new ConcurrentHashMap<>();
    private static final int TEXTURE_SIZE = 18;

    private static int clamp(int val, int min, int max) {
        return Math.max(min, Math.min(max, val));
    }

    private static float clamp(float val, float min, float max) {
        return Math.max(min, Math.min(max, val));
    }

    private static int abgr(int a, int b, int g, int r) {
        return ((a & 0xFF) << 24) | ((b & 0xFF) << 16) | ((g & 0xFF) << 8) | (r & 0xFF);
    }

    public static Identifier getOrCreateShelfTexture(int baseRgb) {
        int rgbKey = baseRgb & 0x00FFFFFF;
        return TEXTURE_CACHE.computeIfAbsent(rgbKey, ShelfTextureManager::generateShelfTexture);
    }

    private static Identifier generateShelfTexture(int rgbKey) {
        NativeImage image = new NativeImage(NativeImage.Format.RGBA, TEXTURE_SIZE, TEXTURE_SIZE, false);

        int baseR = (rgbKey >> 16) & 0xFF;
        int baseG = (rgbKey >> 8) & 0xFF;
        int baseB = rgbKey & 0xFF;

        int sparkleR = 255;
        int sparkleG = 248;
        int sparkleB = 236;

        ModConfig config = ModConfig.get();

        for (int y = 0; y < TEXTURE_SIZE; y++) {
            for (int x = 0; x < TEXTURE_SIZE; x++) {
                if (x == 0 || x == TEXTURE_SIZE - 1 || y == 0 || y == TEXTURE_SIZE - 1) {
                    int r = Math.min(255, (int) (baseR * 1.06f));
                    int g = Math.min(255, (int) (baseG * 1.06f));
                    int b = Math.min(255, (int) (baseB * 1.06f));
                    int a = clamp((int) (config.borderAlpha * 255.0f), 120, 245);
                    image.setPixelABGR(x, y, abgr(a, b, g, r));
                    continue;
                }

                int ix = x - 1;
                int iy = y - 1;

                float cx = 7.5f;
                float cy = 7.5f;
                float dx = (ix - cx) / 7.5f;
                float dy = (iy - cy) / 7.5f;
                float dist = (float) Math.sqrt(dx * dx + dy * dy);

                float circleHalo = 0.0f;
                if (config.haloEffect) {
                    circleHalo = Math.max(0.0f, 1.0f - (dist / 1.05f) * (dist / 1.05f));
                }

                float sparkle = 0.0f;
                if (config.centerGoldAccent) {
                    sparkle = Math.max(0.0f, 1.0f - dist * 1.6f) * config.faintGlowIntensity;
                }

                float insetFactor = 1.0f;
                if (config.insetShading) {
                    if (iy == 0) insetFactor *= 0.74f;
                    if (ix == 0) insetFactor *= 0.78f;
                    if (iy == 15 && ix > 0 && ix < 15) insetFactor *= 1.10f;
                    if (ix == 15 && iy > 0 && iy < 15) insetFactor *= 1.06f;
                }

                float shelfGround = 0.0f;
                if (config.shelfDepth && iy >= 8) {
                    shelfGround = ((iy - 7) / 8.0f) * 0.12f;
                }

                float cr = baseR * (1.0f - sparkle) + sparkleR * sparkle;
                float cg = baseG * (1.0f - sparkle) + sparkleG * sparkle;
                float cb = baseB * (1.0f - sparkle) + sparkleB * sparkle;

                float brightness = (0.92f + circleHalo * 0.22f - shelfGround) * insetFactor;
                brightness = clamp(brightness, 0.45f, 1.25f);

                int finalR = clamp((int) (cr * brightness), 0, 255);
                int finalG = clamp((int) (cg * brightness), 0, 255);
                int finalB = clamp((int) (cb * brightness), 0, 255);

                float innerAlpha = 0.50f + circleHalo * 0.20f + (iy >= 8 ? 0.08f : 0.0f);
                int a = clamp((int) (innerAlpha * 255.0f), 80, 205);

                image.setPixelABGR(x, y, abgr(a, finalB, finalG, finalR));
            }
        }

        String name = "dynamic_cute_shelf_" + Integer.toHexString(rgbKey);
        DynamicTexture texture = new DynamicTexture(() -> name, image);
        Identifier location = Identifier.fromNamespaceAndPath(ItemHighlighterMod.MOD_ID, name);
        Minecraft.getInstance().getTextureManager().register(location, texture);
        return location;
    }

    public static void clearCache() {
        TEXTURE_CACHE.clear();
    }
}
