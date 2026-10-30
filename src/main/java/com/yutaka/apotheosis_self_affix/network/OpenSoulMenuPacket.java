package com.yutaka.apotheosis_self_affix.network;

import com.yutaka.apotheosis_self_affix.menu.SoulMenu;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.NetworkHooks;

import java.util.function.Supplier;

public class OpenSoulMenuPacket {

    public OpenSoulMenuPacket() {
    }

    public OpenSoulMenuPacket(FriendlyByteBuf buf) {
    }

    public void toBytes(FriendlyByteBuf buf) {
    }

    public void handle(Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayer player = ctx.get().getSender();
            if (player != null) {
                NetworkHooks.openScreen(player, new SimpleMenuProvider(
                        (containerId, playerInv, p) -> new SoulMenu(containerId, playerInv),
                        Component.translatable("gui.apotheosis_self_affix.soul_menu")
                ));
            }
        });
        ctx.get().setPacketHandled(true);
    }
}
