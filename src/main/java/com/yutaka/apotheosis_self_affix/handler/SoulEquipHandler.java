package com.yutaka.apotheosis_self_affix.handler;

import com.yutaka.apotheosis_self_affix.ApotheosisSelfAffix;
import com.yutaka.apotheosis_self_affix.config.ModConfig;
import com.yutaka.apotheosis_self_affix.util.SoulAffixHelper;
import net.minecraft.nbt.CompoundTag;
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

    private static final String GIVEN_TAG = "apotheosis_self_affix_given";

    /**
     * Checks if the player has ever been given a soul.
     * Stored under PlayerPersisted so it persists across respawns and deaths.
     */
    public static boolean hasBeenGivenSoul(Player player) {
        CompoundTag forgeData = player.getPersistentData();
        if (forgeData.contains(Player.PERSISTED_NBT_TAG)) {
            CompoundTag persisted = forgeData.getCompound(Player.PERSISTED_NBT_TAG);
            return persisted.getBoolean(GIVEN_TAG);
        }
        return false;
    }

    /**
     * Marks that the player has received their soul.
     */
    public static void markSoulGiven(Player player) {
        CompoundTag forgeData = player.getPersistentData();
        CompoundTag persisted;
        if (forgeData.contains(Player.PERSISTED_NBT_TAG)) {
            persisted = forgeData.getCompound(Player.PERSISTED_NBT_TAG);
        } else {
            persisted = new CompoundTag();
            forgeData.put(Player.PERSISTED_NBT_TAG, persisted);
        }
        persisted.putBoolean(GIVEN_TAG, true);
    }

    /**
     * Checks if the player possesses a Soul anywhere:
     * - Curios soul slot
     * - Any other Curios slot
     * - Main inventory, armor, offhand
     * - Mouse cursor carried stack
     */
    public static boolean hasAnySoulInPossession(Player player) {
        if (player == null) return false;

        // 1. Check Curios inventory
        boolean hasInCurios = CuriosApi.getCuriosInventory(player)
                .map(inv -> inv.findFirstCurio(ApotheosisSelfAffix.SOUL.get()).isPresent())
                .orElse(false);
        if (hasInCurios) return true;

        // 2. Check Player inventory (main, armor, offhand)
        for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
            ItemStack stack = player.getInventory().getItem(i);
            if (!stack.isEmpty() && stack.is(ApotheosisSelfAffix.SOUL.get())) {
                return true;
            }
        }

        // 3. Check cursor stack (e.g. if player is dragging it in inventory)
        ItemStack carried = player.containerMenu.getCarried();
        return !carried.isEmpty() && carried.is(ApotheosisSelfAffix.SOUL.get());
    }

    /**
     * Finds an existing Soul in the player's main inventory, if any.
     */
    public static Optional<ItemStack> findSoulInInventory(Player player) {
        for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
            ItemStack stack = player.getInventory().getItem(i);
            if (!stack.isEmpty() && stack.is(ApotheosisSelfAffix.SOUL.get())) {
                return Optional.of(stack);
            }
        }
        return Optional.empty();
    }

    @SubscribeEvent
    public static void onPlayerLogin(PlayerEvent.PlayerLoggedInEvent event) {
        if (!ModConfig.AUTO_EQUIP_ON_JOIN.get()) return;
        Player player = event.getEntity();
        if (player.level().isClientSide) return;

        // Never generate new soul if already given or already has one
        if (hasBeenGivenSoul(player) || hasAnySoulInPossession(player)) {
            // If soul is in main inventory and soul slot is empty, safely equip existing one
            tryEquipExistingSoulFromInventory(player);
            return;
        }

        equipNewSoulSafely(player);
    }

    @SubscribeEvent
    public static void onPlayerRespawn(PlayerEvent.PlayerRespawnEvent event) {
        if (!ModConfig.AUTO_EQUIP_ON_JOIN.get()) return;
        Player player = event.getEntity();
        if (player.level().isClientSide) return;

        if (hasBeenGivenSoul(player) || hasAnySoulInPossession(player)) {
            tryEquipExistingSoulFromInventory(player);
            return;
        }

        equipNewSoulSafely(player);
    }

    @SubscribeEvent
    public static void onPlayerClone(PlayerEvent.Clone event) {
        Player original = event.getOriginal();
        Player player = event.getEntity();

        if (original.level().isClientSide) return;

        // Preserve given flag across clone
        if (hasBeenGivenSoul(original)) {
            markSoulGiven(player);
        }

        if (!ModConfig.KEEP_ON_DEATH.get()) return;

        // Transfer equipped soul safely across death
        ItemStack originalSoul = SoulAffixHelper.getEquippedSoul(original);
        if (originalSoul.isEmpty()) {
            originalSoul = findSoulInInventory(original).orElse(ItemStack.EMPTY);
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
     * Safety check on player tick: ensures soul is equipped once Curios inventory finishes loading,
     * BUT strictly prevents duplicate generation or overwriting.
     */
    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END || event.player.level().isClientSide) return;
        if (!ModConfig.AUTO_EQUIP_ON_JOIN.get()) return;

        // Check once per 2 seconds (every 40 ticks)
        if (event.player.tickCount % 40 == 0) {
            Player player = event.player;
            if (hasBeenGivenSoul(player) || hasAnySoulInPossession(player)) {
                // If the player has a soul in inventory and the soul slot is empty, move it to the slot
                tryEquipExistingSoulFromInventory(player);
                return;
            }

            // Only equip if truly first time and has no soul anywhere
            equipNewSoulSafely(player);
        }
    }

    /**
     * If the player has a soul in their regular inventory and the soul slot is empty,
     * move the existing (affixed) soul into the soul slot without generating any new item.
     */
    public static void tryEquipExistingSoulFromInventory(Player player) {
        CuriosApi.getCuriosInventory(player).ifPresent(inv -> {
            Optional<SlotResult> slot = inv.findCurio("soul", 0);
            if (slot.isEmpty() || slot.get().stack().isEmpty()) {
                // Find existing soul in inventory
                for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
                    ItemStack invStack = player.getInventory().getItem(i);
                    if (!invStack.isEmpty() && invStack.is(ApotheosisSelfAffix.SOUL.get())) {
                        ItemStack toEquip = invStack.copy();
                        player.getInventory().setItem(i, ItemStack.EMPTY);
                        inv.setEquippedCurio("soul", 0, toEquip);
                        markSoulGiven(player);
                        if (ModConfig.DEBUG_LOG.get()) {
                            ApotheosisSelfAffix.LOGGER.info("Moved existing Soul from inventory to Curios slot for player {}", player.getName().getString());
                        }
                        return;
                    }
                }
            }
        });
    }

    /**
     * Safely equips a brand new soul ONLY when the player has never had one and possesses none.
     */
    public static boolean equipNewSoulSafely(Player player) {
        if (hasBeenGivenSoul(player) || hasAnySoulInPossession(player)) {
            return false;
        }

        return CuriosApi.getCuriosInventory(player).map(inv -> {
            Optional<SlotResult> slot = inv.findCurio("soul", 0);
            if (slot.isPresent() && !slot.get().stack().isEmpty()) {
                // Soul slot already occupied! Never overwrite!
                markSoulGiven(player);
                return false;
            }

            ItemStack newSoul = new ItemStack(ApotheosisSelfAffix.SOUL.get());
            inv.setEquippedCurio("soul", 0, newSoul);
            markSoulGiven(player);

            if (ModConfig.DEBUG_LOG.get()) {
                ApotheosisSelfAffix.LOGGER.info("Equipped brand new initial Soul for first-time player {}", player.getName().getString());
            }
            return true;
        }).orElse(false);
    }
}
