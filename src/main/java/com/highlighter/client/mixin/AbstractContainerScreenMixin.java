package com.highlighter.client.mixin;

import com.highlighter.client.ItemHighlighterClient;
import com.highlighter.config.ModConfig;
import net.minecraft.client.gui.GuiGraphics;
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
     * Injected at HEAD so the 3D illuminated shelf texture renders ON TOP of the GUI slot background,
     * but strictly BEHIND the item (which renders at z=100 later in renderSlot).
     */
    @Inject(method = "renderSlot", at = @At("HEAD"))
    private void onRenderSlot(GuiGraphics graphics, Slot slot, CallbackInfo ci) {
        if (slot != null && slot.container instanceof Inventory) {
            ItemHighlighterClient.renderSlotHighlight(graphics, slot);
        }
    }

    @Inject(method = "render", at = @At("HEAD"))
    private void onRender(GuiGraphics graphics, int mouseX, int mouseY, float partialTick, CallbackInfo ci) {
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
