package com.highlighter.client.mixin;

import com.highlighter.client.ItemHighlighterClient;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerInput;
import net.minecraft.world.inventory.Slot;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(AbstractContainerMenu.class)
public abstract class AbstractContainerMenuMixin {
    @Shadow public abstract Slot getSlot(int slotId);

    @Inject(method = "clicked", at = @At("HEAD"))
    private void onClicked(int slotIndex, int button, ContainerInput containerInput, Player player, CallbackInfo ci) {
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
