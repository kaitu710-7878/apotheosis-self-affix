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
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.item.ItemStack;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.SlotResult;

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
     * Retrieves the equipped Soul item from the player's Curios inventory.
     */
    public static ItemStack getEquippedSoul(LivingEntity entity) {
        if (entity == null) {
            return ItemStack.EMPTY;
        }
        Optional<SlotResult> slot = CuriosApi.getCuriosHelper().findCurio(entity, "soul", 0);
        if (slot.isPresent() && !slot.get().stack().isEmpty()) {
            return slot.get().stack();
        }
        // Fallback: look for the Soul item in any Curios slot
        return CuriosApi.getCuriosHelper()
                .findFirstCurio(entity, ApotheosisSelfAffix.SOUL.get())
                .map(SlotResult::stack)
                .orElse(ItemStack.EMPTY);
    }

    /**
     * Determines whether an affix (from Apotheosis, Apotheotic Additions, Apothic Infinite, Esoteric Reforging, etc.)
     * can be applied to the Soul. Souls accept any affix valid for weapon or armor categories,
     * plus DurableAffix (which functions as experience boost).
     */
    public static boolean canAffixApplyToSoul(Affix affix, ItemStack stack, LootRarity rarity) {
        if (affix == null) {
            return false;
        }

        // Durable affix is accepted as an experience gain booster substitute
        if (affix instanceof DurableAffix) {
            return true;
        }

        // Check if the affix can apply to any weapon or armor category
        for (LootCategory category : ALL_SOUL_CATEGORIES) {
            try {
                if (affix.canApplyTo(stack, category, rarity)) {
                    return true;
                }
            } catch (Exception ignored) {
            }
        }
        return false;
    }

    /**
     * Aggregates all attribute modifiers from all affixes on the Soul item.
     * Evaluates modifiers for MAINHAND (weapons) and CHEST (armor), re-mapping UUIDs to match the Curios slot UUID.
     */
    public static Multimap<Attribute, AttributeModifier> getSoulAttributeModifiers(ItemStack stack, UUID slotUuid) {
        Multimap<Attribute, AttributeModifier> modifiers = HashMultimap.create();
        if (stack.isEmpty() || !AffixHelper.hasAffixes(stack)) {
            return modifiers;
        }

        Map<dev.shadowsoffire.placebo.reload.DynamicHolder<? extends Affix>, AffixInstance> affixes = AffixHelper.getAffixes(stack);
        int index = 0;

        for (AffixInstance inst : affixes.values()) {
            Affix affix = inst.affix().get();
            if (affix == null) continue;

            LootRarity rarity = inst.rarity().get();
            float level = inst.level();

            // Collect modifiers as if equipped in MAINHAND (weapon stats) and CHEST (armor stats)
            for (EquipmentSlot slot : new EquipmentSlot[]{EquipmentSlot.MAINHAND, EquipmentSlot.CHEST}) {
                int finalIndex = index;
                affix.addModifiers(stack, rarity, level, slot, (attr, mod) -> {
                    // Create a deterministic modifier tied to the Curios slot UUID and affix index
                    UUID modUuid = new UUID(slotUuid.getMostSignificantBits(), slotUuid.getLeastSignificantBits() + finalIndex);
                    AttributeModifier curioMod = new AttributeModifier(
                            modUuid,
                            "Soul Affix: " + mod.getName(),
                            mod.getAmount(),
                            mod.getOperation()
                    );
                    modifiers.put(attr, curioMod);
                });
                index++;
            }
        }
        return modifiers;
    }
}
