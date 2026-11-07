package com.highlighter.client.mixin;

import com.highlighter.client.ItemHighlighterClient;
import com.highlighter.config.ModConfig;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Gui.class)
public class GuiMixin {
    /**
     * Injected at HEAD so the 3D illuminated shelf texture renders directly behind the hotbar item.
     */
    @Inject(method = "renderSlot", at = @At("HEAD"))
    private void onRenderSlot(GuiGraphics graphics, int x, int y, DeltaTracker deltaTracker, Player player, ItemStack item, int seed, CallbackInfo ci) {
        if (player == null || item == null || item.isEmpty()) {
            return;
        }

        // Check if this item is in the player's hotbar (slots 0 to 8)
        for (int i = 0; i < 9; i++) {
            if (player.getInventory().items.get(i) == item) {
                if (ModConfig.get().clearOnSelect && player.getInventory().selected == i) {
                    ItemHighlighterClient.clearMark(i);
                } else {
                    ItemHighlighterClient.renderHotbarHighlight(graphics, i, x, y, item);
                }
                break;
            }
        }
    }
}
