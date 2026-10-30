package com.yutaka.apotheosis_self_affix.manager;

import com.google.common.collect.Multimap;
import com.yutaka.apotheosis_self_affix.capability.ISoulHandler;
import com.yutaka.apotheosis_self_affix.capability.SoulCapabilityProvider;
import com.yutaka.apotheosis_self_affix.network.ModPacketHandler;
import com.yutaka.apotheosis_self_affix.network.SyncSoulPacket;
import com.yutaka.apotheosis_self_affix.util.SoulAffixHelper;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class SoulAttributeManager {

    public static final UUID SOUL_SLOT_UUID = UUID.fromString("44444444-5555-6666-7777-888888888888");
    public static final String MODIFIER_PREFIX = "Soul Affix:";

    /**
     * Re-calculates and applies attribute modifiers from the player's Soul slot
     * directly to the player's AttributeMap as transient modifiers.
     */
    public static void updateSoulAttributes(Player player) {
        if (player == null || player.level().isClientSide) {
            return;
        }

        // 1. Remove existing soul affix modifiers from the player
        for (AttributeInstance instance : player.getAttributes().getSyncableAttributes()) {
            removeSoulModifiers(instance);
        }

        // 2. Retrieve soul item from custom soul capability
        ItemStack soul = player.getCapability(SoulCapabilityProvider.SOUL_CAPABILITY)
                .map(ISoulHandler::getSoul)
                .orElse(ItemStack.EMPTY);

        if (!soul.isEmpty()) {
            Multimap<Attribute, AttributeModifier> modifiers = SoulAffixHelper.getSoulAttributeModifiers(soul, SOUL_SLOT_UUID);
            for (Map.Entry<Attribute, AttributeModifier> entry : modifiers.entries()) {
                Attribute attr = entry.getKey();
                AttributeModifier mod = entry.getValue();
                AttributeInstance instance = player.getAttribute(attr);
                if (instance != null) {
                    // Remove if somehow already exists, then add transient
                    if (instance.hasModifier(mod)) {
                        instance.removeModifier(mod.getId());
                    }
                    instance.addTransientModifier(mod);
                }
            }
        }

        // 3. Sync to client if this is a ServerPlayer
        if (player instanceof ServerPlayer serverPlayer) {
            ModPacketHandler.sendToPlayer(serverPlayer, new SyncSoulPacket(soul));
        }
    }

    private static void removeSoulModifiers(AttributeInstance instance) {
        if (instance == null) return;
        List<UUID> toRemove = new ArrayList<>();
        for (AttributeModifier mod : instance.getModifiers()) {
            if (mod.getName().startsWith(MODIFIER_PREFIX)) {
                toRemove.add(mod.getId());
            }
        }
        for (UUID uuid : toRemove) {
            instance.removeModifier(uuid);
        }
    }
}
