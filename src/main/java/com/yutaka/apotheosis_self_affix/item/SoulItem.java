package com.yutaka.apotheosis_self_affix.item;

import com.google.common.collect.Multimap;
import com.yutaka.apotheosis_self_affix.config.ModConfig;
import com.yutaka.apotheosis_self_affix.util.SoulAffixHelper;
import dev.shadowsoffire.apotheosis.adventure.affix.Affix;
import dev.shadowsoffire.apotheosis.adventure.affix.AffixHelper;
import dev.shadowsoffire.apotheosis.adventure.affix.AffixInstance;
import dev.shadowsoffire.apotheosis.adventure.affix.effect.DurableAffix;
import dev.shadowsoffire.apotheosis.adventure.loot.LootRarity;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;
import top.theillusivec4.curios.api.SlotContext;
import top.theillusivec4.curios.api.type.capability.ICurio;
import top.theillusivec4.curios.api.type.capability.ICurioItem;

import java.util.*;

public class SoulItem extends Item implements ICurioItem {

    private static final UUID DUMMY_UUID = UUID.fromString("11111111-2222-3333-4444-555555555555");

    public SoulItem() {
        super(new Item.Properties()
                .stacksTo(1)
                .fireResistant()
                .rarity(Rarity.EPIC));
    }

    @Override
    public ICurio.DropRule getDropRule(SlotContext slotContext, DamageSource source, int lootingLevel, boolean recentlyHit, ItemStack stack) {
        if (ModConfig.KEEP_ON_DEATH.get()) {
            return ICurio.DropRule.ALWAYS_KEEP;
        }
        return ICurio.DropRule.DEFAULT;
    }

    @Override
    public boolean canEquip(SlotContext slotContext, ItemStack stack) {
        return "soul".equals(slotContext.identifier());
    }

    @Override
    public Multimap<Attribute, AttributeModifier> getAttributeModifiers(SlotContext slotContext, UUID uuid, ItemStack stack) {
        return SoulAffixHelper.getSoulAttributeModifiers(stack, uuid);
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltips, TooltipFlag flag) {
        tooltips.add(Component.translatable("tooltip.apotheosis_self_affix.soul_desc").withStyle(ChatFormatting.DARK_AQUA));
        if (ModConfig.KEEP_ON_DEATH.get()) {
            tooltips.add(Component.translatable("tooltip.apotheosis_self_affix.keep_on_death").withStyle(ChatFormatting.LIGHT_PURPLE));
        }

        if (AffixHelper.hasAffixes(stack)) {
            dev.shadowsoffire.placebo.reload.DynamicHolder<LootRarity> rarityHolder = AffixHelper.getRarity(stack);
            LootRarity rarity = rarityHolder != null ? rarityHolder.get() : null;
            Map<dev.shadowsoffire.placebo.reload.DynamicHolder<? extends Affix>, AffixInstance> affixes = AffixHelper.getAffixes(stack);

            tooltips.add(Component.empty());

            // 1. Ability affixes description
            for (AffixInstance inst : affixes.values()) {
                Affix affix = inst.affix().get();
                if (affix == null) continue;

                if (affix instanceof DurableAffix durable) {
                    float lvl = inst.level();
                    float durabilityBonus = durable.getDurabilityBonusPercentage(stack, rarity, lvl, null);
                    float xpBonus = (float) (durabilityBonus * 100.0f * ModConfig.DURABLE_XP_MULTIPLIER.get());
                    String formatted = String.format("%.1f", xpBonus);
                    tooltips.add(Component.translatable("affix.apotheosis_self_affix.durable_xp_substitute", formatted).withStyle(ChatFormatting.GREEN));
                } else {
                    MutableComponent desc = inst.getDescription();
                    if (desc != null && !desc.getString().isEmpty()) {
                        tooltips.add(desc);
                    }
                }
            }

            // 2. Attribute Modifiers explicitly listed for Curios tooltips
            Multimap<Attribute, AttributeModifier> modifiers = SoulAffixHelper.getSoulAttributeModifiers(stack, DUMMY_UUID);
            if (!modifiers.isEmpty()) {
                tooltips.add(Component.translatable("tooltip.apotheosis_self_affix.attributes_header").withStyle(ChatFormatting.GOLD));
                for (Map.Entry<Attribute, AttributeModifier> entry : modifiers.entries()) {
                    Attribute attr = entry.getKey();
                    AttributeModifier mod = entry.getValue();
                    double amount = mod.getAmount();
                    if (Math.abs(amount) < 0.0001) continue;

                    String valueStr;
                    if (mod.getOperation() == AttributeModifier.Operation.ADDITION) {
                        valueStr = (amount > 0 ? "+" : "") + String.format(Locale.ROOT, "%.1f", amount);
                    } else {
                        valueStr = (amount > 0 ? "+" : "") + String.format(Locale.ROOT, "%.1f%%", amount * 100.0);
                    }

                    tooltips.add(Component.literal("  " + valueStr + " ")
                            .withStyle(amount > 0 ? ChatFormatting.BLUE : ChatFormatting.RED)
                            .append(Component.translatable(attr.getDescriptionId()).withStyle(ChatFormatting.GRAY)));
                }
            }
        }

        super.appendHoverText(stack, level, tooltips, flag);
    }

    @Override
    public boolean isFoil(ItemStack stack) {
        return super.isFoil(stack) || AffixHelper.hasAffixes(stack);
    }
}
