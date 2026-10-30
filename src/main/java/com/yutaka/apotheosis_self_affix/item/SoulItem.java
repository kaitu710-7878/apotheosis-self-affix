package com.yutaka.apotheosis_self_affix.item;

import com.google.common.collect.Multimap;
import com.yutaka.apotheosis_self_affix.config.ModConfig;
import com.yutaka.apotheosis_self_affix.util.SoulAffixHelper;
import dev.shadowsoffire.apotheosis.adventure.affix.AffixHelper;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EquipmentSlot;
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

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.List;
import java.util.Locale;
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
    public Multimap<Attribute, AttributeModifier> getAttributeModifiers(net.minecraft.world.entity.EquipmentSlot slot, ItemStack stack) {
        // Explicitly return empty multimap for all vanilla equipment slots (head, chest, legs, feet, mainhand, offhand)
        // because Soul is purely a Curios accessory. This prevents duplicate slot tooltip sections.
        return com.google.common.collect.ImmutableMultimap.of();
    }

    private static final DecimalFormat ATTRIBUTE_FORMAT = new DecimalFormat("#.##", DecimalFormatSymbols.getInstance(Locale.ROOT));
    private static final UUID DUMMY_UUID = UUID.fromString("11111111-2222-3333-4444-555555555555");

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltips, TooltipFlag flag) {
        tooltips.add(Component.translatable("tooltip.apotheosis_self_affix.soul_desc").withStyle(ChatFormatting.DARK_AQUA));
        if (ModConfig.KEEP_ON_DEATH.get()) {
            tooltips.add(Component.translatable("tooltip.apotheosis_self_affix.keep_on_death").withStyle(ChatFormatting.LIGHT_PURPLE));
        }

        // Render Curios Soul slot attribute modifiers
        if (AffixHelper.hasAffixes(stack)) {
            Multimap<Attribute, AttributeModifier> modifiers = SoulAffixHelper.getSoulAttributeModifiers(stack, DUMMY_UUID);
            if (!modifiers.isEmpty()) {
                tooltips.add(Component.empty());
                tooltips.add(Component.translatable("curios.modifiers.soul").withStyle(ChatFormatting.GOLD));
                for (Map.Entry<Attribute, AttributeModifier> entry : modifiers.entries()) {
                    Attribute attr = entry.getKey();
                    AttributeModifier mod = entry.getValue();
                    double amount = mod.getAmount();
                    if (Math.abs(amount) < 0.0001) continue;

                    double displayAmount = amount;
                    String formattedNum;
                    if (mod.getOperation() == AttributeModifier.Operation.ADDITION) {
                        formattedNum = (displayAmount > 0 ? "+" : "") + ATTRIBUTE_FORMAT.format(displayAmount);
                    } else {
                        displayAmount = amount * 100.0;
                        formattedNum = (displayAmount > 0 ? "+" : "") + ATTRIBUTE_FORMAT.format(displayAmount) + "%";
                    }

                    MutableComponent attrComp = Component.translatable(attr.getDescriptionId());
                    MutableComponent line = Component.literal("  " + formattedNum + " ").append(attrComp);
                    if (amount > 0) {
                        line.withStyle(ChatFormatting.BLUE);
                    } else {
                        line.withStyle(ChatFormatting.RED);
                    }
                    tooltips.add(line);
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
