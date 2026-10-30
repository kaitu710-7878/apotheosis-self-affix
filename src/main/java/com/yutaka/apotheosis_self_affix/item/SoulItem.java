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

import java.util.List;
import java.util.Map;
import java.util.UUID;

public class SoulItem extends Item implements ICurioItem {

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

        // Show Durable affix conversion to XP bonus
        if (AffixHelper.hasAffixes(stack)) {
            Map<dev.shadowsoffire.placebo.reload.DynamicHolder<? extends Affix>, AffixInstance> affixes = AffixHelper.getAffixes(stack);
            for (AffixInstance inst : affixes.values()) {
                if (inst.affix().get() instanceof DurableAffix durable) {
                    LootRarity rarity = inst.rarity().get();
                    float lvl = inst.level();
                    float durabilityBonus = durable.getDurabilityBonusPercentage(stack, rarity, lvl, null);
                    float xpBonus = (float) (durabilityBonus * 100.0f * ModConfig.DURABLE_XP_MULTIPLIER.get());
                    String formatted = String.format("%.1f", xpBonus);
                    tooltips.add(Component.translatable("affix.apotheosis_self_affix.durable_xp_substitute", formatted).withStyle(ChatFormatting.GREEN));
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
