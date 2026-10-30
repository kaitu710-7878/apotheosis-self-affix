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
        if (!ModConfig.AUTO_EQUIP_ON_JOIN.get()) {
            return;
        }
        Player player = event.getEntity();
        if (player.level().isClientSide) {
            return;
        }

        equipSoulIfMissing(player);
    }

    @SubscribeEvent
    public static void onPlayerRespawn(PlayerEvent.PlayerRespawnEvent event) {
        Player player = event.getEntity();
        if (player.level().isClientSide) {
            return;
        }
        equipSoulIfMissing(player);
    }

    @SubscribeEvent
    public static void onPlayerClone(PlayerEvent.Clone event) {
        if (!ModConfig.KEEP_ON_DEATH.get()) {
            return;
        }

        Player original = event.getOriginal();
        Player player = event.getEntity();

        if (original.level().isClientSide) {
            return;
        }

        // Retrieve Soul item from the original player
        ItemStack originalSoul = SoulAffixHelper.getEquippedSoul(original);
        if (!originalSoul.isEmpty()) {
            ItemStack preservedSoul = originalSoul.copy();
            CuriosApi.getCuriosHelper().setEquippedCurio(player, "soul", 0, preservedSoul);
            if (ModConfig.DEBUG_LOG.get()) {
                ApotheosisSelfAffix.LOGGER.info("Preserved and transferred Soul across death for player {}", player.getName().getString());
            }
        }
    }

    private static void equipSoulIfMissing(Player player) {
        Optional<SlotResult> slot = CuriosApi.getCuriosHelper().findCurio(player, "soul", 0);
        if (slot.isEmpty() || slot.get().stack().isEmpty()) {
            // Also check if they already have one in inventory or other curio slot
            ItemStack existing = SoulAffixHelper.getEquippedSoul(player);
            if (existing.isEmpty()) {
                ItemStack newSoul = new ItemStack(ApotheosisSelfAffix.SOUL.get());
                CuriosApi.getCuriosHelper().setEquippedCurio(player, "soul", 0, newSoul);
                if (ModConfig.DEBUG_LOG.get()) {
                    ApotheosisSelfAffix.LOGGER.info("Equipped initial Soul for player {}", player.getName().getString());
                }
            }
        }
    }
}
