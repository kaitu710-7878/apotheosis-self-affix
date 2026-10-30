package com.yutaka.apotheosis_self_affix.mixin;

import com.yutaka.apotheosis_self_affix.config.ModConfig;
import com.yutaka.apotheosis_self_affix.item.SoulItem;
import dev.shadowsoffire.apotheosis.adventure.affix.effect.DurableAffix;
import dev.shadowsoffire.apotheosis.adventure.loot.LootRarity;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = DurableAffix.class, remap = false)
public class DurableAffixMixin {

    @Inject(method = "getDescription", at = @At("HEAD"), cancellable = true, remap = false)
    private void getDescriptionForSoul(ItemStack stack, LootRarity rarity, float level, CallbackInfoReturnable<MutableComponent> cir) {
        if (stack.getItem() instanceof SoulItem) {
            DurableAffix affix = (DurableAffix) (Object) this;
            float durabilityBonus = affix.getDurabilityBonusPercentage(stack, rarity, level, null);
            float xpBonus = (float) (durabilityBonus * 100.0f * ModConfig.DURABLE_XP_MULTIPLIER.get());
            String formatted = String.format("%.1f", xpBonus);
            cir.setReturnValue(Component.translatable("affix.apotheosis_self_affix.durable_xp_substitute", formatted).withStyle(ChatFormatting.GREEN));
        }
    }

    @Inject(method = "getAugmentingText", at = @At("HEAD"), cancellable = true, remap = false)
    private void getAugmentingTextForSoul(ItemStack stack, LootRarity rarity, float level, CallbackInfoReturnable<Component> cir) {
        if (stack.getItem() instanceof SoulItem) {
            DurableAffix affix = (DurableAffix) (Object) this;
            float durabilityBonus = affix.getDurabilityBonusPercentage(stack, rarity, level, null);
            float xpBonus = (float) (durabilityBonus * 100.0f * ModConfig.DURABLE_XP_MULTIPLIER.get());
            String formatted = String.format("%.1f", xpBonus);
            cir.setReturnValue(Component.translatable("affix.apotheosis_self_affix.durable_xp_substitute", formatted).withStyle(ChatFormatting.GREEN));
        }
    }
}
