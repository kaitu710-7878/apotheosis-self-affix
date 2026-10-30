package com.yutaka.apotheosis_self_affix.handler;

import com.yutaka.apotheosis_self_affix.ApotheosisSelfAffix;
import com.yutaka.apotheosis_self_affix.config.ModConfig;
import com.yutaka.apotheosis_self_affix.util.SoulAffixHelper;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.TickEvent;
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

        equipSoulSafely(player);
    }

    @SubscribeEvent
    public static void onPlayerRespawn(PlayerEvent.PlayerRespawnEvent event) {
        if (!ModConfig.AUTO_EQUIP_ON_JOIN.get()) return;
        Player player = event.getEntity();
        if (player.level().isClientSide) return;

        equipSoulSafely(player);
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
            // Check inventory of original player
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

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END || event.player.level().isClientSide) return;
        if (!ModConfig.AUTO_EQUIP_ON_JOIN.get()) return;

        // Safety check every 2 seconds (40 ticks)
        if (event.player.tickCount % 40 == 0) {
            equipSoulSafely(event.player);
        }
    }

    /**
     * Equips a soul safely:
     * 1. NEVER overwrites if the soul slot already contains an item.
     * 2. If the soul slot is empty, first checks if the player already carries a soul in inventory.
     *    If so, equips that existing (affixed) soul into the slot without creating a duplicate.
     * 3. If the soul slot is empty and no soul is in inventory (lost/dropped/inaccessible),
     *    creates and equips a new soul as a recovery mechanism (per user specification).
     */
    public static boolean equipSoulSafely(Player player) {
        if (player == null) return false;

        return CuriosApi.getCuriosInventory(player).map(inv -> {
            Optional<SlotResult> slot = inv.findCurio("soul", 0);

            // 1. NEVER overwrite: if slot is already occupied, leave it completely alone!
            if (slot.isPresent() && !slot.get().stack().isEmpty()) {
                return false;
            }

            // 2. Check if player has an existing soul in inventory
            for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
                ItemStack invStack = player.getInventory().getItem(i);
                if (!invStack.isEmpty() && invStack.is(ApotheosisSelfAffix.SOUL.get())) {
                    ItemStack toEquip = invStack.copy();
                    player.getInventory().setItem(i, ItemStack.EMPTY);
                    inv.setEquippedCurio("soul", 0, toEquip);
                    if (ModConfig.DEBUG_LOG.get()) {
                        ApotheosisSelfAffix.LOGGER.info("Moved existing Soul from inventory to Soul slot for player {}", player.getName().getString());
                    }
                    return true;
                }
            }

            // 3. No soul in slot, no soul in inventory -> relief / recovery mechanism generates a new soul
            ItemStack newSoul = new ItemStack(ApotheosisSelfAffix.SOUL.get());
            inv.setEquippedCurio("soul", 0, newSoul);
            if (ModConfig.DEBUG_LOG.get()) {
                ApotheosisSelfAffix.LOGGER.info("Supplied recovery/initial Soul for player {}", player.getName().getString());
            }
            return true;
        }).orElse(false);
    }
}
