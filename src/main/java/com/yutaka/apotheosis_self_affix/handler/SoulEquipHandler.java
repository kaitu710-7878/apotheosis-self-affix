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

    private static final String GIVEN_TAG = "apotheosis_self_affix_given";

    @SubscribeEvent
    public static void onPlayerLogin(PlayerEvent.PlayerLoggedInEvent event) {
        if (!ModConfig.AUTO_EQUIP_ON_JOIN.get()) return;
        Player player = event.getEntity();
        if (player.level().isClientSide) return;

        equipSoulIfMissing(player);
    }

    @SubscribeEvent
    public static void onPlayerRespawn(PlayerEvent.PlayerRespawnEvent event) {
        Player player = event.getEntity();
        if (player.level().isClientSide) return;

        equipSoulIfMissing(player);
    }

    @SubscribeEvent
    public static void onPlayerClone(PlayerEvent.Clone event) {
        if (!ModConfig.KEEP_ON_DEATH.get()) return;

        Player original = event.getOriginal();
        Player player = event.getEntity();

        if (original.level().isClientSide) return;

        ItemStack originalSoul = SoulAffixHelper.getEquippedSoul(original);
        if (!originalSoul.isEmpty()) {
            ItemStack preservedSoul = originalSoul.copy();
            CuriosApi.getCuriosInventory(player).ifPresent(inv -> {
                inv.setEquippedCurio("soul", 0, preservedSoul);
            });
            if (ModConfig.DEBUG_LOG.get()) {
                ApotheosisSelfAffix.LOGGER.info("Preserved and transferred Soul across death for player {}", player.getName().getString());
            }
        }
    }

    /**
     * Safety check on player tick: ensures soul is equipped once Curios inventory finishes loading.
     */
    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END || event.player.level().isClientSide) return;
        if (!ModConfig.AUTO_EQUIP_ON_JOIN.get()) return;

        // Check once per second (every 20 ticks) if player somehow missed the soul
        if (event.player.tickCount % 20 == 0) {
            if (!event.player.getPersistentData().getBoolean(GIVEN_TAG)) {
                if (equipSoulIfMissing(event.player)) {
                    event.player.getPersistentData().putBoolean(GIVEN_TAG, true);
                }
            }
        }
    }

    public static boolean equipSoulIfMissing(Player player) {
        return CuriosApi.getCuriosInventory(player).map(inv -> {
            Optional<SlotResult> slot = inv.findCurio("soul", 0);
            if (slot.isEmpty() || slot.get().stack().isEmpty()) {
                ItemStack existing = SoulAffixHelper.getEquippedSoul(player);
                if (existing.isEmpty()) {
                    ItemStack newSoul = new ItemStack(ApotheosisSelfAffix.SOUL.get());
                    inv.setEquippedCurio("soul", 0, newSoul);
                    if (ModConfig.DEBUG_LOG.get()) {
                        ApotheosisSelfAffix.LOGGER.info("Equipped initial Soul for player {}", player.getName().getString());
                    }
                    return true;
                }
            }
            return false;
        }).orElse(false);
    }
}
