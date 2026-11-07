package com.highlighter.client.mixin;

import com.highlighter.client.ItemHighlighterClient;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.network.protocol.game.ClientboundTakeItemEntityPacket;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientPacketListener.class)
public class ClientPacketListenerMixin {
    @Inject(method = "handleTakeItemEntity", at = @At("HEAD"))
    private void onItemPickup(ClientboundTakeItemEntityPacket packet, CallbackInfo ci) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player != null && mc.level != null && packet.getPlayerId() == mc.player.getId()) {
            Entity entity = mc.level.getEntity(packet.getItemId());
            if (entity instanceof ItemEntity itemEntity) {
                ItemHighlighterClient.onPreItemPickup(itemEntity.getItem());
            }
        }
    }
}
