package com.yutaka.apotheosis_self_affix.handler;

import com.yutaka.apotheosis_self_affix.ApotheosisSelfAffix;
import com.yutaka.apotheosis_self_affix.capability.ISoulHandler;
import com.yutaka.apotheosis_self_affix.capability.SoulCapabilityProvider;
import com.yutaka.apotheosis_self_affix.config.ModConfig;
import com.yutaka.apotheosis_self_affix.manager.SoulAttributeManager;
import com.yutaka.apotheosis_self_affix.util.SoulAffixHelper;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = ApotheosisSelfAffix.MODID)
public class SoulEquipHandler {

    /**
     * Attaches the dedicated Soul Capability to every player.
     */
    @SubscribeEvent
    public static void onAttachCapabilities(AttachCapabilitiesEvent<Entity> event) {
        if (event.getObject() instanceof Player player) {
            if (!event.getCapabilities().containsKey(SoulCapabilityProvider.IDENTIFIER)) {
                event.addCapability(SoulCapabilityProvider.IDENTIFIER,
                        new SoulCapabilityProvider(() -> SoulAttributeManager.updateSoulAttributes(player)));
            }
        }
    }

    @SubscribeEvent
    public static void onPlayerLogin(PlayerEvent.PlayerLoggedInEvent event) {
        Player player = event.getEntity();
        if (player.level().isClientSide) return;

        if (ModConfig.AUTO_EQUIP_ON_JOIN.get()) {
            equipSoulIfCompletelyMissing(player);
        }
        SoulAttributeManager.updateSoulAttributes(player);
    }

    @SubscribeEvent
    public static void onPlayerRespawn(PlayerEvent.PlayerRespawnEvent event) {
        Player player = event.getEntity();
        if (player.level().isClientSide) return;

        if (ModConfig.AUTO_EQUIP_ON_JOIN.get()) {
            equipSoulIfCompletelyMissing(player);
        }
        SoulAttributeManager.updateSoulAttributes(player);
    }

    @SubscribeEvent
    public static void onPlayerChangedDimension(PlayerEvent.PlayerChangedDimensionEvent event) {
        Player player = event.getEntity();
        if (player.level().isClientSide) return;

        SoulAttributeManager.updateSoulAttributes(player);
    }

    @SubscribeEvent
    public static void onPlayerClone(PlayerEvent.Clone event) {
        Player original = event.getOriginal();
        Player player = event.getEntity();

        // Copy capability across player cloning (death / dimension transition)
        original.reviveCaps();
        original.getCapability(SoulCapabilityProvider.SOUL_CAPABILITY).ifPresent(oldHandler -> {
            player.getCapability(SoulCapabilityProvider.SOUL_CAPABILITY).ifPresent(newHandler -> {
                if (event.isWasDeath()) {
                    if (ModConfig.KEEP_ON_DEATH.get()) {
                        newHandler.deserializeNBT(oldHandler.serializeNBT());
                        if (ModConfig.DEBUG_LOG.get()) {
                            ApotheosisSelfAffix.LOGGER.info("Preserved and transferred Soul across death for player {}", player.getName().getString());
                        }
                    }
                } else {
                    // Normal dimension travel / return from End
                    newHandler.deserializeNBT(oldHandler.serializeNBT());
                }
            });
        });
        original.invalidateCaps();

        // Handle keep on death from inventory if soul was in player inventory when killed
        if (event.isWasDeath() && ModConfig.KEEP_ON_DEATH.get()) {
            ItemStack preservedSoul = ItemStack.EMPTY;
            for (int i = 0; i < original.getInventory().getContainerSize(); i++) {
                ItemStack stack = original.getInventory().getItem(i);
                if (!stack.isEmpty() && stack.is(ApotheosisSelfAffix.SOUL.get())) {
                    preservedSoul = stack.copy();
                    break;
                }
            }
            if (!preservedSoul.isEmpty()) {
                final ItemStack finalPreserved = preservedSoul;
                player.getCapability(SoulCapabilityProvider.SOUL_CAPABILITY).ifPresent(handler -> {
                    if (handler.getSoul().isEmpty()) {
                        handler.setSoul(finalPreserved);
                    }
                });
            }
        }
    }

    /**
     * Checks if the player has a soul ANYWHERE in their possession:
     * - Dedicated soul slot capability
     * - Main inventory, armor, offhand
     */
    public static boolean hasSoulInPossession(Player player) {
        if (player == null) return false;

        // 1. Check dedicated soul capability
        boolean hasInSlot = player.getCapability(SoulCapabilityProvider.SOUL_CAPABILITY)
                .map(ISoulHandler::hasSoul)
                .orElse(false);
        if (hasInSlot) return true;

        // 2. Check Player inventory
        for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
            ItemStack stack = player.getInventory().getItem(i);
            if (!stack.isEmpty() && stack.is(ApotheosisSelfAffix.SOUL.get())) {
                return true;
            }
        }

        return false;
    }

    /**
     * Supplies a relief soul ONLY when the player possesses no soul anywhere.
     * Never overwrites existing equipped soul.
     * Never duplicates a soul if the player has one in their inventory.
     */
    public static boolean equipSoulIfCompletelyMissing(Player player) {
        if (player == null) return false;

        if (hasSoulInPossession(player)) {
            return false;
        }

        // Only when completely missing -> supply relief soul in dedicated slot
        return player.getCapability(SoulCapabilityProvider.SOUL_CAPABILITY).map(handler -> {
            if (!handler.hasSoul()) {
                ItemStack newSoul = new ItemStack(ApotheosisSelfAffix.SOUL.get());
                handler.setSoul(newSoul);
                if (ModConfig.DEBUG_LOG.get()) {
                    ApotheosisSelfAffix.LOGGER.info("Supplied relief Soul for player {}", player.getName().getString());
                }
                return true;
            }
            return false;
        }).orElse(false);
    }
}
