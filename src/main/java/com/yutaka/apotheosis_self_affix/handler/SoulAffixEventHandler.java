package com.yutaka.apotheosis_self_affix.handler;

import com.yutaka.apotheosis_self_affix.ApotheosisSelfAffix;
import com.yutaka.apotheosis_self_affix.config.ModConfig;
import com.yutaka.apotheosis_self_affix.util.SoulAffixHelper;
import dev.shadowsoffire.apotheosis.adventure.affix.Affix;
import dev.shadowsoffire.apotheosis.adventure.affix.AffixHelper;
import dev.shadowsoffire.apotheosis.adventure.affix.AffixInstance;
import dev.shadowsoffire.apotheosis.adventure.affix.effect.DamageReductionAffix;
import dev.shadowsoffire.apotheosis.adventure.affix.effect.DurableAffix;
import dev.shadowsoffire.apotheosis.adventure.loot.LootRarity;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.entity.living.LivingDamageEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.event.entity.player.PlayerXpEvent;
import net.minecraftforge.event.level.BlockEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.Map;

@Mod.EventBusSubscriber(modid = ApotheosisSelfAffix.MODID)
public class SoulAffixEventHandler {

    /**
     * Handles defense/armor affixes when the player is hurt (damage reduction, onHurt potions/effects).
     */
    @SubscribeEvent(priority = EventPriority.HIGH)
    public static void onLivingHurt(LivingHurtEvent event) {
        LivingEntity victim = event.getEntity();
        if (victim.level().isClientSide) return;

        // Process defensive affixes for the victim
        ItemStack soul = SoulAffixHelper.getEquippedSoul(victim);
        if (!soul.isEmpty() && AffixHelper.hasAffixes(soul)) {
            Map<dev.shadowsoffire.placebo.reload.DynamicHolder<? extends Affix>, AffixInstance> affixes = AffixHelper.getAffixes(soul);
            DamageSource source = event.getSource();
            float currentDamage = event.getAmount();

            for (AffixInstance inst : affixes.values()) {
                Affix affix = inst.affix().get();
                if (affix == null) continue;
                LootRarity rarity = inst.rarity().get();
                float level = inst.level();

                // Apply damage reduction protection
                int prot = affix.getDamageProtection(soul, rarity, level, source);
                if (prot > 0) {
                    float reductionFactor = 1.0f - Math.min(20.0f, prot) * 0.04f;
                    currentDamage *= Math.max(0.0f, reductionFactor);
                }

                // Call onHurt for custom damage reduction or triggers
                currentDamage = affix.onHurt(soul, rarity, level, source, victim, currentDamage);
            }
            event.setAmount(Math.max(0.0f, currentDamage));
        }

        // Process offensive affixes for the attacker (damage bonus)
        Entity directAttacker = event.getSource().getEntity();
        if (directAttacker instanceof LivingEntity attacker) {
            ItemStack attackerSoul = SoulAffixHelper.getEquippedSoul(attacker);
            if (!attackerSoul.isEmpty() && AffixHelper.hasAffixes(attackerSoul)) {
                Map<dev.shadowsoffire.placebo.reload.DynamicHolder<? extends Affix>, AffixInstance> affixes = AffixHelper.getAffixes(attackerSoul);
                float bonusDamage = 0.0f;
                for (AffixInstance inst : affixes.values()) {
                    Affix affix = inst.affix().get();
                    if (affix == null) continue;
                    LootRarity rarity = inst.rarity().get();
                    float level = inst.level();

                    bonusDamage += affix.getDamageBonus(soul, rarity, level, victim.getMobType());
                }
                if (bonusDamage > 0.0f) {
                    event.setAmount(event.getAmount() + bonusDamage);
                }
            }
        }
    }

    /**
     * Handles post-hurt and post-attack affixes (potions on hurt, potions on attack, cleave, thunder, etc.)
     */
    @SubscribeEvent(priority = EventPriority.LOW)
    public static void onLivingDamage(LivingDamageEvent event) {
        LivingEntity victim = event.getEntity();
        if (victim.level().isClientSide) return;

        Entity directAttacker = event.getSource().getEntity();

        // 1. Post-Hurt triggers on the victim (e.g. hurt-self potion, counter-blindness, thorns-like effects)
        ItemStack victimSoul = SoulAffixHelper.getEquippedSoul(victim);
        if (!victimSoul.isEmpty() && AffixHelper.hasAffixes(victimSoul)) {
            Map<dev.shadowsoffire.placebo.reload.DynamicHolder<? extends Affix>, AffixInstance> affixes = AffixHelper.getAffixes(victimSoul);
            for (AffixInstance inst : affixes.values()) {
                Affix affix = inst.affix().get();
                if (affix == null) continue;
                LootRarity rarity = inst.rarity().get();
                float level = inst.level();

                affix.doPostHurt(victimSoul, rarity, level, victim, directAttacker);
            }
        }

        // 2. Post-Attack triggers on the attacker (e.g. attack potions, cleaving, executing, thunderstruck)
        if (directAttacker instanceof LivingEntity attacker) {
            ItemStack attackerSoul = SoulAffixHelper.getEquippedSoul(attacker);
            if (!attackerSoul.isEmpty() && AffixHelper.hasAffixes(attackerSoul)) {
                Map<dev.shadowsoffire.placebo.reload.DynamicHolder<? extends Affix>, AffixInstance> affixes = AffixHelper.getAffixes(attackerSoul);
                for (AffixInstance inst : affixes.values()) {
                    Affix affix = inst.affix().get();
                    if (affix == null) continue;
                    LootRarity rarity = inst.rarity().get();
                    float level = inst.level();

                    affix.doPostAttack(attackerSoul, rarity, level, attacker, victim);
                }
            }
        }
    }

    /**
     * Replaces durability bonus (Durable affix) with bonus experience gain when picking up XP.
     */
    @SubscribeEvent
    public static void onPickupXp(PlayerXpEvent.PickupXp event) {
        Player player = event.getEntity();
        if (player.level().isClientSide) return;

        ItemStack soul = SoulAffixHelper.getEquippedSoul(player);
        if (!soul.isEmpty() && AffixHelper.hasAffixes(soul)) {
            Map<dev.shadowsoffire.placebo.reload.DynamicHolder<? extends Affix>, AffixInstance> affixes = AffixHelper.getAffixes(soul);
            float totalBonusRate = 0.0f;

            for (AffixInstance inst : affixes.values()) {
                if (inst.affix().get() instanceof DurableAffix durable) {
                    LootRarity rarity = inst.rarity().get();
                    float level = inst.level();
                    float durabilityBonus = durable.getDurabilityBonusPercentage(soul, rarity, level, null);
                    totalBonusRate += durabilityBonus;
                }
            }

            if (totalBonusRate > 0.0f) {
                double multiplier = ModConfig.DURABLE_XP_MULTIPLIER.get();
                int baseValue = event.getOrb().getValue();
                int bonusXp = (int) Math.round(baseValue * totalBonusRate * multiplier);
                if (bonusXp > 0) {
                    player.giveExperiencePoints(bonusXp);
                    if (ModConfig.DEBUG_LOG.get()) {
                        ApotheosisSelfAffix.LOGGER.info("Granted {} bonus XP (substitute for Durable affix) to player {}",
                                bonusXp, player.getName().getString());
                    }
                }
            }
        }
    }

    /**
     * Handles block breaking affixes (e.g. Telepathic, etc.)
     */
    @SubscribeEvent
    public static void onBlockBreak(BlockEvent.BreakEvent event) {
        Player player = event.getPlayer();
        if (player == null || player.level().isClientSide) return;

        ItemStack soul = SoulAffixHelper.getEquippedSoul(player);
        if (!soul.isEmpty() && AffixHelper.hasAffixes(soul)) {
            Map<dev.shadowsoffire.placebo.reload.DynamicHolder<? extends Affix>, AffixInstance> affixes = AffixHelper.getAffixes(soul);
            for (AffixInstance inst : affixes.values()) {
                Affix affix = inst.affix().get();
                if (affix == null) continue;
                LootRarity rarity = inst.rarity().get();
                float level = inst.level();

                affix.onBlockBreak(soul, rarity, level, player, event.getLevel(), event.getPos(), event.getState());
            }
        }
    }
}
