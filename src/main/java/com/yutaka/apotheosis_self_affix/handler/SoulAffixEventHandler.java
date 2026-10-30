package com.yutaka.apotheosis_self_affix.handler;

import com.yutaka.apotheosis_self_affix.ApotheosisSelfAffix;
import com.yutaka.apotheosis_self_affix.config.ModConfig;
import com.yutaka.apotheosis_self_affix.util.SoulAffixHelper;
import dev.shadowsoffire.apotheosis.adventure.affix.Affix;
import dev.shadowsoffire.apotheosis.adventure.affix.AffixHelper;
import dev.shadowsoffire.apotheosis.adventure.affix.AffixInstance;
import dev.shadowsoffire.apotheosis.adventure.affix.effect.DurableAffix;
import dev.shadowsoffire.apotheosis.adventure.loot.LootRarity;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.HitResult;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.event.entity.ProjectileImpactEvent;
import net.minecraftforge.event.entity.living.LivingDamageEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.event.entity.living.ShieldBlockEvent;
import net.minecraftforge.event.entity.player.PlayerXpEvent;
import net.minecraftforge.event.level.BlockEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.Map;

@Mod.EventBusSubscriber(modid = ApotheosisSelfAffix.MODID)
public class SoulAffixEventHandler {

    /**
     * Handles defense/armor affixes when the player is hurt (damage reduction, onHurt potions/effects),
     * and offensive damage bonuses when the player attacks an entity.
     */
    @SubscribeEvent(priority = EventPriority.HIGH)
    public static void onLivingHurt(LivingHurtEvent event) {
        LivingEntity victim = event.getEntity();
        if (victim.level().isClientSide) return;

        // 1. Process defensive affixes for the victim
        ItemStack victimSoul = SoulAffixHelper.getEquippedSoul(victim);
        if (!victimSoul.isEmpty() && AffixHelper.hasAffixes(victimSoul)) {
            Map<dev.shadowsoffire.placebo.reload.DynamicHolder<? extends Affix>, AffixInstance> affixes = SoulAffixHelper.getAffixesSafe(victimSoul);
            DamageSource source = event.getSource();
            float currentDamage = event.getAmount();

            for (AffixInstance inst : affixes.values()) {
                Affix affix = inst.affix().get();
                if (affix == null) continue;
                LootRarity rarity = inst.rarity().get();
                float level = inst.level();

                // Apply damage reduction protection
                int prot = SoulAffixHelper.getSafeDamageProtection(affix, victimSoul, rarity, level, source);
                if (prot > 0) {
                    float reductionFactor = 1.0f - Math.min(20.0f, prot) * 0.04f;
                    currentDamage *= Math.max(0.0f, reductionFactor);
                }

                // Call onHurt for custom damage reduction or triggers
                currentDamage = SoulAffixHelper.safeOnHurt(affix, victimSoul, rarity, level, source, victim, currentDamage);
            }
            event.setAmount(Math.max(0.0f, currentDamage));
        }

        // 2. Process offensive affixes for the attacker (damage bonus)
        Entity directAttacker = event.getSource().getEntity();
        if (directAttacker instanceof LivingEntity attacker) {
            ItemStack attackerSoul = SoulAffixHelper.getEquippedSoul(attacker);
            if (!attackerSoul.isEmpty() && AffixHelper.hasAffixes(attackerSoul)) {
                Map<dev.shadowsoffire.placebo.reload.DynamicHolder<? extends Affix>, AffixInstance> affixes = SoulAffixHelper.getAffixesSafe(attackerSoul);
                float bonusDamage = 0.0f;
                for (AffixInstance inst : affixes.values()) {
                    Affix affix = inst.affix().get();
                    if (affix == null) continue;
                    LootRarity rarity = inst.rarity().get();
                    float level = inst.level();

                    bonusDamage += SoulAffixHelper.getSafeDamageBonus(affix, attackerSoul, rarity, level, victim.getMobType());
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

        // 1. Post-Hurt triggers on the victim (e.g. hurt-self potion, counter-blindness, thorns)
        ItemStack victimSoul = SoulAffixHelper.getEquippedSoul(victim);
        if (!victimSoul.isEmpty() && AffixHelper.hasAffixes(victimSoul)) {
            Map<dev.shadowsoffire.placebo.reload.DynamicHolder<? extends Affix>, AffixInstance> affixes = SoulAffixHelper.getAffixesSafe(victimSoul);
            for (AffixInstance inst : affixes.values()) {
                Affix affix = inst.affix().get();
                if (affix == null) continue;
                LootRarity rarity = inst.rarity().get();
                float level = inst.level();

                SoulAffixHelper.safeDoPostHurt(affix, victimSoul, rarity, level, victim, directAttacker);
            }
        }

        // 2. Post-Attack triggers on the attacker (e.g. attack potions, cleaving, executing, thunderstruck)
        if (directAttacker instanceof LivingEntity attacker) {
            ItemStack attackerSoul = SoulAffixHelper.getEquippedSoul(attacker);
            if (!attackerSoul.isEmpty() && AffixHelper.hasAffixes(attackerSoul)) {
                Map<dev.shadowsoffire.placebo.reload.DynamicHolder<? extends Affix>, AffixInstance> affixes = SoulAffixHelper.getAffixesSafe(attackerSoul);
                for (AffixInstance inst : affixes.values()) {
                    Affix affix = inst.affix().get();
                    if (affix == null) continue;
                    LootRarity rarity = inst.rarity().get();
                    float level = inst.level();

                    SoulAffixHelper.safeDoPostAttack(affix, attackerSoul, rarity, level, attacker, victim);
                }
            }
        }
    }

    /**
     * Handles shield blocking affixes (Psychic, etc.)
     */
    @SubscribeEvent
    public static void onShieldBlock(ShieldBlockEvent event) {
        LivingEntity blocker = event.getEntity();
        if (blocker.level().isClientSide) return;

        ItemStack soul = SoulAffixHelper.getEquippedSoul(blocker);
        if (!soul.isEmpty() && AffixHelper.hasAffixes(soul)) {
            Map<dev.shadowsoffire.placebo.reload.DynamicHolder<? extends Affix>, AffixInstance> affixes = SoulAffixHelper.getAffixesSafe(soul);
            float blocked = event.getBlockedDamage();

            for (AffixInstance inst : affixes.values()) {
                Affix affix = inst.affix().get();
                if (affix == null) continue;
                LootRarity rarity = inst.rarity().get();
                float level = inst.level();

                blocked = SoulAffixHelper.safeOnShieldBlock(affix, soul, rarity, level, blocker, event.getDamageSource(), blocked);
            }
            event.setBlockedDamage(blocked);
        }
    }

    /**
     * Handles arrow firing affixes when a projectile spawned by a soul-wearing entity enters the level.
     */
    @SubscribeEvent
    public static void onArrowFired(EntityJoinLevelEvent event) {
        if (event.getLevel().isClientSide) return;
        if (event.getEntity() instanceof AbstractArrow arrow) {
            if (arrow.getOwner() instanceof LivingEntity shooter) {
                ItemStack soul = SoulAffixHelper.getEquippedSoul(shooter);
                if (!soul.isEmpty() && AffixHelper.hasAffixes(soul)) {
                    Map<dev.shadowsoffire.placebo.reload.DynamicHolder<? extends Affix>, AffixInstance> affixes = SoulAffixHelper.getAffixesSafe(soul);
                    for (AffixInstance inst : affixes.values()) {
                        Affix affix = inst.affix().get();
                        if (affix == null) continue;
                        LootRarity rarity = inst.rarity().get();
                        float level = inst.level();

                        SoulAffixHelper.safeOnArrowFired(affix, soul, rarity, level, shooter, arrow);
                    }
                }
            }
        }
    }

    /**
     * Handles arrow impact affixes.
     */
    @SubscribeEvent
    public static void onArrowImpact(ProjectileImpactEvent event) {
        if (event.getEntity().level().isClientSide) return;
        if (event.getEntity() instanceof AbstractArrow arrow) {
            if (arrow.getOwner() instanceof LivingEntity shooter) {
                ItemStack soul = SoulAffixHelper.getEquippedSoul(shooter);
                if (!soul.isEmpty() && AffixHelper.hasAffixes(soul)) {
                    Map<dev.shadowsoffire.placebo.reload.DynamicHolder<? extends Affix>, AffixInstance> affixes = SoulAffixHelper.getAffixesSafe(soul);
                    for (AffixInstance inst : affixes.values()) {
                        Affix affix = inst.affix().get();
                        if (affix == null) continue;
                        LootRarity rarity = inst.rarity().get();
                        float level = inst.level();

                        HitResult result = event.getRayTraceResult();
                        SoulAffixHelper.safeOnArrowImpact(affix, rarity, level, arrow, result, result.getType());
                    }
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
            Map<dev.shadowsoffire.placebo.reload.DynamicHolder<? extends Affix>, AffixInstance> affixes = SoulAffixHelper.getAffixesSafe(soul);
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
            Map<dev.shadowsoffire.placebo.reload.DynamicHolder<? extends Affix>, AffixInstance> affixes = SoulAffixHelper.getAffixesSafe(soul);
            for (AffixInstance inst : affixes.values()) {
                Affix affix = inst.affix().get();
                if (affix == null) continue;
                LootRarity rarity = inst.rarity().get();
                float level = inst.level();

                SoulAffixHelper.safeOnBlockBreak(affix, soul, rarity, level, player, event.getLevel(), event.getPos(), event.getState());
            }
        }
    }
}
