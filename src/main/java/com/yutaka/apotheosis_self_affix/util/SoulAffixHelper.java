package com.yutaka.apotheosis_self_affix.util;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.yutaka.apotheosis_self_affix.ApotheosisSelfAffix;
import dev.shadowsoffire.apotheosis.adventure.affix.Affix;
import dev.shadowsoffire.apotheosis.adventure.affix.AffixHelper;
import dev.shadowsoffire.apotheosis.adventure.affix.AffixInstance;
import dev.shadowsoffire.apotheosis.adventure.affix.effect.DurableAffix;
import dev.shadowsoffire.apotheosis.adventure.loot.LootCategory;
import dev.shadowsoffire.apotheosis.adventure.loot.LootRarity;
import dev.shadowsoffire.apotheosis.adventure.loot.RarityRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobType;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.HitResult;
import com.yutaka.apotheosis_self_affix.capability.ISoulHandler;
import com.yutaka.apotheosis_self_affix.capability.SoulCapabilityProvider;

import java.util.*;

public class SoulAffixHelper {

    public static final List<LootCategory> WEAPON_CATEGORIES = List.of(
            LootCategory.SWORD,
            LootCategory.HEAVY_WEAPON,
            LootCategory.BOW,
            LootCategory.CROSSBOW,
            LootCategory.TRIDENT
    );

    public static final List<LootCategory> ARMOR_CATEGORIES = List.of(
            LootCategory.HELMET,
            LootCategory.CHESTPLATE,
            LootCategory.LEGGINGS,
            LootCategory.BOOTS,
            LootCategory.SHIELD
    );

    public static final List<LootCategory> ALL_SOUL_CATEGORIES;

    static {
        List<LootCategory> list = new ArrayList<>();
        list.addAll(WEAPON_CATEGORIES);
        list.addAll(ARMOR_CATEGORIES);
        ALL_SOUL_CATEGORIES = Collections.unmodifiableList(list);
    }

    /**
     * Retrieves the equipped Soul item from the entity's dedicated Soul capability.
     */
    public static ItemStack getEquippedSoul(LivingEntity entity) {
        if (entity instanceof Player player) {
            return player.getCapability(SoulCapabilityProvider.SOUL_CAPABILITY)
                    .map(ISoulHandler::getSoul)
                    .orElse(ItemStack.EMPTY);
        }
        return ItemStack.EMPTY;
    }

    /**
     * Finds the most suitable fallback rarity if the given affix does not natively
     * support targetRarity (e.g. for esoteric/artifact/heirloom rarities).
     */
    public static LootRarity getEffectiveRarity(Affix affix, LootRarity targetRarity) {
        if (affix == null || targetRarity == null) {
            return targetRarity;
        }

        // Test if the affix directly supports targetRarity
        if (doesAffixSupportRarity(affix, targetRarity)) {
            return targetRarity;
        }

        // Search backward from targetRarity using prev()
        LootRarity current = targetRarity;
        for (int i = 0; i < 10; i++) {
            LootRarity prev = current.prev();
            if (prev == null || prev == current) break;
            if (doesAffixSupportRarity(affix, prev)) {
                return prev;
            }
            current = prev;
        }

        // Fallback search across all known rarities in descending order of quality
        List<LootRarity> rarities = new ArrayList<>(RarityRegistry.INSTANCE.getValues());
        rarities.sort((a, b) -> Integer.compare(b.ordinal(), a.ordinal()));
        for (LootRarity r : rarities) {
            if (r.ordinal() <= targetRarity.ordinal() && doesAffixSupportRarity(affix, r)) {
                return r;
            }
        }

        // Default to targetRarity if nothing else matched
        return targetRarity;
    }

    /**
     * Checks if the affix can be applied under the specified rarity for any soul category.
     */
    public static boolean doesAffixSupportRarity(Affix affix, LootRarity rarity) {
        if (affix == null || rarity == null) return false;
        if (affix instanceof DurableAffix) return true;

        for (EquipmentSlot slot : EquipmentSlot.values()) {
            ItemStack dummy = getRepresentativeStack(slot);
            for (LootCategory category : ALL_SOUL_CATEGORIES) {
                try {
                    if (affix.canApplyTo(dummy, category, rarity)) {
                        return true;
                    }
                } catch (Exception ignored) {
                }
            }
        }
        return false;
    }

    /**
     * Determines whether an affix can be applied to the Soul.
     * Souls accept any affix valid for weapon or armor categories,
     * including custom rarities via fallback check.
     */
    public static boolean canAffixApplyToSoul(Affix affix, ItemStack stack, LootRarity rarity) {
        if (affix == null) {
            return false;
        }

        if (affix instanceof DurableAffix) {
            return true;
        }

        // 1. Direct check with the requested rarity
        for (LootCategory category : ALL_SOUL_CATEGORIES) {
            try {
                if (affix.canApplyTo(stack, category, rarity)) {
                    return true;
                }
            } catch (Exception ignored) {
            }
        }

        // 2. Fallback check for custom tiers (e.g. esoteric, artifact, heirloom)
        LootRarity effective = getEffectiveRarity(affix, rarity);
        if (effective != null && effective != rarity) {
            for (LootCategory category : ALL_SOUL_CATEGORIES) {
                try {
                    if (affix.canApplyTo(stack, category, effective)) {
                        return true;
                    }
                } catch (Exception ignored) {
                }
            }
        }

        return false;
    }

    /**
     * Safely retrieves all affixes from the Soul stack, with fallback to direct NBT parsing
     * if Apotheosis's cached map was cleared or failed category validation.
     */
    public static Map<dev.shadowsoffire.placebo.reload.DynamicHolder<? extends Affix>, AffixInstance> getAffixesSafe(ItemStack stack) {
        if (stack.isEmpty() || !stack.hasTag()) {
            return Collections.emptyMap();
        }

        Map<dev.shadowsoffire.placebo.reload.DynamicHolder<? extends Affix>, AffixInstance> affixes = AffixHelper.getAffixes(stack);
        if (!affixes.isEmpty()) {
            return affixes;
        }

        // Direct fallback from NBT if AffixHelper.getAffixes returns empty
        net.minecraft.nbt.CompoundTag afxData = stack.getTagElement(AffixHelper.AFFIX_DATA);
        if (afxData == null || !afxData.contains(AffixHelper.AFFIXES)) {
            return Collections.emptyMap();
        }

        net.minecraft.nbt.CompoundTag affixesTag = afxData.getCompound(AffixHelper.AFFIXES);
        dev.shadowsoffire.placebo.reload.DynamicHolder<LootRarity> rarityHolder = AffixHelper.getRarity(afxData);
        if (!rarityHolder.isBound()) {
            rarityHolder = RarityRegistry.getMinRarity();
        }

        Map<dev.shadowsoffire.placebo.reload.DynamicHolder<? extends Affix>, AffixInstance> map = new HashMap<>();
        for (String key : affixesTag.getAllKeys()) {
            dev.shadowsoffire.placebo.reload.DynamicHolder<Affix> holder = dev.shadowsoffire.apotheosis.adventure.affix.AffixRegistry.INSTANCE.holder(new net.minecraft.resources.ResourceLocation(key));
            if (holder.isBound()) {
                float level = affixesTag.getFloat(key);
                map.put(holder, new AffixInstance(holder, stack, rarityHolder, level));
            }
        }
        return map;
    }

    private static java.lang.reflect.Field ATTRIBUTE_AFFIX_MODIFIERS_FIELD = null;

    static {
        try {
            ATTRIBUTE_AFFIX_MODIFIERS_FIELD = dev.shadowsoffire.apotheosis.adventure.affix.AttributeAffix.class.getDeclaredField("modifiers");
            ATTRIBUTE_AFFIX_MODIFIERS_FIELD.setAccessible(true);
        } catch (Exception ignored) {
        }
    }

    /**
     * Aggregates all attribute modifiers from all affixes on the Soul item.
     * Handles custom rarity scaling (e.g. Esoteric) and ensures proper Curios application.
     */
    public static Multimap<Attribute, AttributeModifier> getSoulAttributeModifiers(ItemStack stack, UUID slotUuid) {
        Multimap<Attribute, AttributeModifier> modifiers = HashMultimap.create();
        if (stack.isEmpty() || !AffixHelper.hasAffixes(stack)) {
            return modifiers;
        }

        Map<dev.shadowsoffire.placebo.reload.DynamicHolder<? extends Affix>, AffixInstance> affixes = getAffixesSafe(stack);
        int index = 0;
        Set<String> addedNames = new HashSet<>();

        for (AffixInstance inst : affixes.values()) {
            Affix affix = inst.affix().get();
            if (affix == null) continue;

            LootRarity originalRarity = inst.rarity().get();
            LootRarity effectiveRarity = getEffectiveRarity(affix, originalRarity);
            float level = inst.level();

            // Calculate scaling factor if original rarity is higher than effective rarity
            double scale = 1.0;
            if (originalRarity != null && effectiveRarity != null && originalRarity.ordinal() > effectiveRarity.ordinal()) {
                int tierDiff = originalRarity.ordinal() - effectiveRarity.ordinal();
                scale = 1.0 + (tierDiff * 0.15); // +15% per tier above effective
            }

            final double finalScale = scale;
            boolean resolved = false;

            // Strategy 1: Direct extraction from AttributeAffix via reflection
            // This directly gets the attribute, operation, and computed value without relying on category slot matching.
            if (affix instanceof dev.shadowsoffire.apotheosis.adventure.affix.AttributeAffix && ATTRIBUTE_AFFIX_MODIFIERS_FIELD != null) {
                try {
                    @SuppressWarnings("unchecked")
                    Map<LootRarity, dev.shadowsoffire.apotheosis.adventure.affix.AttributeAffix.ModifierInst> map =
                            (Map<LootRarity, dev.shadowsoffire.apotheosis.adventure.affix.AttributeAffix.ModifierInst>) ATTRIBUTE_AFFIX_MODIFIERS_FIELD.get(affix);
                    if (map != null) {
                        dev.shadowsoffire.apotheosis.adventure.affix.AttributeAffix.ModifierInst modInst = map.get(effectiveRarity);
                        if (modInst != null && modInst.attr() != null) {
                            Attribute attr = modInst.attr();
                            AttributeModifier.Operation op = modInst.op();
                            double rawValue = modInst.valueFactory().get(level);
                            double scaledAmount = rawValue * finalScale;
                            String key = attr.getDescriptionId() + ":" + op.name() + ":" + affix.getId();
                            if (addedNames.add(key)) {
                                UUID modUuid = new UUID(slotUuid.getMostSignificantBits(), slotUuid.getLeastSignificantBits() + index + key.hashCode());
                                AttributeModifier curioMod = new AttributeModifier(
                                        modUuid,
                                        "Soul Affix: " + affix.getId().getPath(),
                                        scaledAmount,
                                        op
                                );
                                modifiers.put(attr, curioMod);
                                resolved = true;
                            }
                        }
                    }
                } catch (Exception ignored) {
                }
            }

            // Strategy 2: Fallback using representative dummy item stacks for each equipment slot.
            // Bypasses the fact that LootCategory.forItem(SoulItem).getSlots() is empty.
            if (!resolved) {
                for (EquipmentSlot slot : EquipmentSlot.values()) {
                    ItemStack dummyStack = getRepresentativeStack(slot);
                    int finalIndex = index;
                    affix.addModifiers(dummyStack, effectiveRarity, level, slot, (attr, mod) -> {
                        String key = attr.getDescriptionId() + ":" + mod.getOperation().name() + ":" + mod.getName();
                        if (!addedNames.add(key)) {
                            return; // Prevent duplicate modifiers
                        }

                        UUID modUuid = new UUID(slotUuid.getMostSignificantBits(), slotUuid.getLeastSignificantBits() + finalIndex + key.hashCode());
                        double scaledAmount = mod.getAmount() * finalScale;
                        AttributeModifier curioMod = new AttributeModifier(
                                modUuid,
                                "Soul Affix: " + mod.getName(),
                                scaledAmount,
                                mod.getOperation()
                        );
                        modifiers.put(attr, curioMod);
                    });
                }
            }
            index++;
        }
        return modifiers;
    }

    private static ItemStack getRepresentativeStack(EquipmentSlot slot) {
        return switch (slot) {
            case HEAD -> new ItemStack(net.minecraft.world.item.Items.DIAMOND_HELMET);
            case CHEST -> new ItemStack(net.minecraft.world.item.Items.DIAMOND_CHESTPLATE);
            case LEGS -> new ItemStack(net.minecraft.world.item.Items.DIAMOND_LEGGINGS);
            case FEET -> new ItemStack(net.minecraft.world.item.Items.DIAMOND_BOOTS);
            case MAINHAND -> new ItemStack(net.minecraft.world.item.Items.DIAMOND_SWORD);
            case OFFHAND -> new ItemStack(net.minecraft.world.item.Items.SHIELD);
        };
    }

    // ==========================================
    // Safe Invocation Helpers for Affix Effects
    // ==========================================

    public static int getSafeDamageProtection(Affix affix, ItemStack soul, LootRarity rarity, float level, DamageSource source) {
        try {
            LootRarity eff = getEffectiveRarity(affix, rarity);
            return affix.getDamageProtection(soul, eff, level, source);
        } catch (Exception e) {
            ApotheosisSelfAffix.LOGGER.debug("Error in affix getDamageProtection: {}", e.getMessage());
            return 0;
        }
    }

    public static float getSafeDamageBonus(Affix affix, ItemStack soul, LootRarity rarity, float level, MobType mobType) {
        try {
            LootRarity eff = getEffectiveRarity(affix, rarity);
            return affix.getDamageBonus(soul, eff, level, mobType);
        } catch (Exception e) {
            ApotheosisSelfAffix.LOGGER.debug("Error in affix getDamageBonus: {}", e.getMessage());
            return 0.0f;
        }
    }

    public static float safeOnHurt(Affix affix, ItemStack soul, LootRarity rarity, float level, DamageSource source, LivingEntity entity, float damage) {
        try {
            LootRarity eff = getEffectiveRarity(affix, rarity);
            return affix.onHurt(soul, eff, level, source, entity, damage);
        } catch (Exception e) {
            ApotheosisSelfAffix.LOGGER.debug("Error in affix onHurt: {}", e.getMessage());
            return damage;
        }
    }

    public static void safeDoPostHurt(Affix affix, ItemStack soul, LootRarity rarity, float level, LivingEntity victim, Entity attacker) {
        try {
            LootRarity eff = getEffectiveRarity(affix, rarity);
            affix.doPostHurt(soul, eff, level, victim, attacker);
        } catch (Exception e) {
            ApotheosisSelfAffix.LOGGER.debug("Error in affix doPostHurt: {}", e.getMessage());
        }
    }

    public static void safeDoPostAttack(Affix affix, ItemStack soul, LootRarity rarity, float level, LivingEntity attacker, Entity target) {
        try {
            LootRarity eff = getEffectiveRarity(affix, rarity);
            affix.doPostAttack(soul, eff, level, attacker, target);
        } catch (Exception e) {
            ApotheosisSelfAffix.LOGGER.debug("Error in affix doPostAttack: {}", e.getMessage());
        }
    }

    public static float safeOnShieldBlock(Affix affix, ItemStack soul, LootRarity rarity, float level, LivingEntity blocker, DamageSource source, float blocked) {
        try {
            LootRarity eff = getEffectiveRarity(affix, rarity);
            return affix.onShieldBlock(soul, eff, level, blocker, source, blocked);
        } catch (Exception e) {
            ApotheosisSelfAffix.LOGGER.debug("Error in affix onShieldBlock: {}", e.getMessage());
            return blocked;
        }
    }

    public static void safeOnArrowFired(Affix affix, ItemStack soul, LootRarity rarity, float level, LivingEntity shooter, AbstractArrow arrow) {
        try {
            LootRarity eff = getEffectiveRarity(affix, rarity);
            affix.onArrowFired(soul, eff, level, shooter, arrow);
        } catch (Exception e) {
            ApotheosisSelfAffix.LOGGER.debug("Error in affix onArrowFired: {}", e.getMessage());
        }
    }

    public static void safeOnArrowImpact(Affix affix, LootRarity rarity, float level, AbstractArrow arrow, HitResult result, HitResult.Type type) {
        try {
            LootRarity eff = getEffectiveRarity(affix, rarity);
            affix.onArrowImpact(arrow, eff, level, result, type);
        } catch (Exception e) {
            ApotheosisSelfAffix.LOGGER.debug("Error in affix onArrowImpact: {}", e.getMessage());
        }
    }

    public static void safeOnBlockBreak(Affix affix, ItemStack soul, LootRarity rarity, float level, Player player, LevelAccessor levelAccessor, BlockPos pos, BlockState state) {
        try {
            LootRarity eff = getEffectiveRarity(affix, rarity);
            affix.onBlockBreak(soul, eff, level, player, levelAccessor, pos, state);
        } catch (Exception e) {
            ApotheosisSelfAffix.LOGGER.debug("Error in affix onBlockBreak: {}", e.getMessage());
        }
    }
}
