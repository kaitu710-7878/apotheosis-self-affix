package com.yutaka.apotheosis_self_affix.handler;

import com.yutaka.apotheosis_self_affix.ApotheosisSelfAffix;
import com.yutaka.apotheosis_self_affix.config.ModConfig;
import com.yutaka.apotheosis_self_affix.util.SoulAffixHelper;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.SlotResult;

import java.util.Optional;

@Mod.EventBusSubscriber(modid = ApotheosisSelfAffix.MODID)
public class SoulEquipHandler {

    @SubscribeEvent
    public static void onPlayerLogin(PlayerEvent.PlayerLoggedInEvent event) {
        if (!ModConfig.AUTO_EQUIP_ON_JOIN.get()) return;
        Player player = event.getEntity();
        if (player.level().isClientSide) return;

        equipSoulIfCompletelyMissing(player);
    }

    @SubscribeEvent
    public static void onPlayerRespawn(PlayerEvent.PlayerRespawnEvent event) {
        if (!ModConfig.AUTO_EQUIP_ON_JOIN.get()) return;
        Player player = event.getEntity();
        if (player.level().isClientSide) return;

        equipSoulIfCompletelyMissing(player);
    }

    @SubscribeEvent
    public static void onPlayerClone(PlayerEvent.Clone event) {
        if (!ModConfig.KEEP_ON_DEATH.get()) return;

        Player original = event.getOriginal();
        Player player = event.getEntity();
        if (original.level().isClientSide) return;

        // Safely transfer the original soul across death clone
        ItemStack originalSoul = SoulAffixHelper.getEquippedSoul(original);
        if (originalSoul.isEmpty()) {
            for (int i = 0; i < original.getInventory().getContainerSize(); i++) {
                ItemStack stack = original.getInventory().getItem(i);
                if (!stack.isEmpty() && stack.is(ApotheosisSelfAffix.SOUL.get())) {
                    originalSoul = stack;
                    break;
                }
            }
        }

        if (!originalSoul.isEmpty()) {
            ItemStack preservedSoul = originalSoul.copy();
            CuriosApi.getCuriosInventory(player).ifPresent(inv -> {
                Optional<SlotResult> slot = inv.findCurio("soul", 0);
                if (slot.isEmpty() || slot.get().stack().isEmpty()) {
                    inv.setEquippedCurio("soul", 0, preservedSoul);
                    if (ModConfig.DEBUG_LOG.get()) {
                        ApotheosisSelfAffix.LOGGER.info("Preserved and transferred Soul across death for player {}", player.getName().getString());
                    }
                }
            });
        }
    }

    /**
     * Checks if the player has a soul ANYWHERE in their possession:
     * - Curios soul slot
     * - Main inventory, armor, offhand
     */
    public static boolean hasSoulInPossession(Player player) {
        if (player == null) return false;

        // 1. Check Curios soul slot
        boolean hasInCurios = CuriosApi.getCuriosInventory(player)
                .map(inv -> {
                    Optional<SlotResult> slot = inv.findCurio("soul", 0);
                    return slot.isPresent() && !slot.get().stack().isEmpty();
                })
                .orElse(false);
        if (hasInCurios) return true;

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

        // If player already has a soul in slot or inventory, do NOTHING! No duplicates, no overwrites.
        if (hasSoulInPossession(player)) {
            return false;
        }

        // Only when completely missing (lost, dropped, etc.) -> supply relief soul in Curios slot
        return CuriosApi.getCuriosInventory(player).map(inv -> {
            Optional<SlotResult> slot = inv.findCurio("soul", 0);
            if (slot.isEmpty() || slot.get().stack().isEmpty()) {
                ItemStack newSoul = new ItemStack(ApotheosisSelfAffix.SOUL.get());
                inv.setEquippedCurio("soul", 0, newSoul);
                if (ModConfig.DEBUG_LOG.get()) {
                    ApotheosisSelfAffix.LOGGER.info("Supplied relief Soul for player {}", player.getName().getString());
                }
                return true;
            }
            return false;
        }).orElse(false);
    }
}
