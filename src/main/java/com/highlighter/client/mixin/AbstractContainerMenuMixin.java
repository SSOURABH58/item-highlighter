package com.highlighter.client.mixin;

import com.highlighter.client.ItemHighlighterClient;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.Slot;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(AbstractContainerMenu.class)
public abstract class AbstractContainerMenuMixin {
    @Shadow public abstract Slot getSlot(int slotId);

    @Inject(method = "doClick", at = @At("HEAD"))
    private void onDoClick(int slotIndex, int button, ClickType clickType, Player player, CallbackInfo ci) {
        if (slotIndex >= 0) {
            try {
                Slot slot = this.getSlot(slotIndex);
                if (slot != null && slot.container instanceof Inventory) {
                    ItemHighlighterClient.onSlotClicked(slot);
                }
            } catch (Exception ignored) {
            }
        }
    }
}
