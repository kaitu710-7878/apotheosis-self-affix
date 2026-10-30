package com.yutaka.apotheosis_self_affix.network;

import com.yutaka.apotheosis_self_affix.capability.ISoulHandler;
import com.yutaka.apotheosis_self_affix.capability.SoulCapabilityProvider;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class SyncSoulPacket {

    private final ItemStack soul;

    public SyncSoulPacket(ItemStack soul) {
        this.soul = soul.copy();
    }

    public SyncSoulPacket(FriendlyByteBuf buf) {
        this.soul = buf.readItem();
    }

    public void toBytes(FriendlyByteBuf buf) {
        buf.writeItem(soul);
    }

    public void handle(Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            Player player = Minecraft.getInstance().player;
            if (player != null) {
                player.getCapability(SoulCapabilityProvider.SOUL_CAPABILITY).ifPresent(handler -> {
                    handler.setSoul(soul);
                });
            }
        });
        ctx.get().setPacketHandled(true);
    }
}
