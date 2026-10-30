package com.yutaka.apotheosis_self_affix.network;

import com.yutaka.apotheosis_self_affix.ApotheosisSelfAffix;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.simple.SimpleChannel;

public class ModPacketHandler {

    private static final String PROTOCOL_VERSION = "1";
    public static final SimpleChannel INSTANCE = NetworkRegistry.newSimpleChannel(
            new ResourceLocation(ApotheosisSelfAffix.MODID, "main"),
            () -> PROTOCOL_VERSION,
            PROTOCOL_VERSION::equals,
            PROTOCOL_VERSION::equals
    );

    private static int packetId = 0;

    private static int nextId() {
        return packetId++;
    }

    public static void register() {
        INSTANCE.messageBuilder(SyncSoulPacket.class, nextId(), NetworkDirection.PLAY_TO_CLIENT)
                .decoder(SyncSoulPacket::new)
                .encoder(SyncSoulPacket::toBytes)
                .consumerMainThread(SyncSoulPacket::handle)
                .add();

        INSTANCE.messageBuilder(OpenSoulMenuPacket.class, nextId(), NetworkDirection.PLAY_TO_SERVER)
                .decoder(OpenSoulMenuPacket::new)
                .encoder(OpenSoulMenuPacket::toBytes)
                .consumerMainThread(OpenSoulMenuPacket::handle)
                .add();
    }

    public static void sendToPlayer(ServerPlayer player, Object packet) {
        INSTANCE.send(PacketDistributor.PLAYER.with(() -> player), packet);
    }

    public static void sendToServer(Object packet) {
        INSTANCE.sendToServer(packet);
    }
}
