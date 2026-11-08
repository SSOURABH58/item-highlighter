package com.highlighter.client.mixin;

import com.highlighter.client.ItemHighlighterClient;
import com.highlighter.config.ModConfig;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(AbstractContainerScreen.class)
public abstract class AbstractContainerScreenMixin extends Screen {
    @Shadow protected Slot hoveredSlot;

    protected AbstractContainerScreenMixin(Component title) {
        super(title);
    }

    /**
     * Injected at HEAD of extractSlot so the 3D illuminated shelf renders ON TOP of the slot background,
     * but strictly BEHIND the item sprite and stack count.
     */
    @Inject(method = "extractSlot", at = @At("HEAD"))
    private void onExtractSlot(GuiGraphicsExtractor extractor, Slot slot, int mouseX, int mouseY, CallbackInfo ci) {
        if (slot != null && slot.container instanceof Inventory) {
            ItemHighlighterClient.renderSlotHighlight(extractor, slot);
        }
    }

    @Inject(method = "extractRenderState", at = @At("HEAD"))
    private void onExtractRenderState(GuiGraphicsExtractor extractor, int mouseX, int mouseY, float partialTick, CallbackInfo ci) {
        if (this.hoveredSlot != null && this.hoveredSlot.container instanceof Inventory) {
            ItemHighlighterClient.onSlotHovered(this.hoveredSlot);
        }
    }

    @Inject(method = "onClose", at = @At("HEAD"))
    private void onScreenClose(CallbackInfo ci) {
        if (ModConfig.get().clearOnInventoryClose) {
            ItemHighlighterClient.clearAll();
        }
    }
}
