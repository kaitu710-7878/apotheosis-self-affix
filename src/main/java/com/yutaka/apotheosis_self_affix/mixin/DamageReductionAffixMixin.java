package com.yutaka.apotheosis_self_affix.mixin;

import com.yutaka.apotheosis_self_affix.util.SoulAffixHelper;
import dev.shadowsoffire.apotheosis.adventure.affix.effect.DamageReductionAffix;
import dev.shadowsoffire.apotheosis.adventure.loot.LootRarity;
import dev.shadowsoffire.placebo.util.StepFunction;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Map;

@Mixin(value = DamageReductionAffix.class, remap = false)
public class DamageReductionAffixMixin {

    @Shadow
    protected Map<LootRarity, StepFunction> values;

    @Inject(method = "getDescription", at = @At("HEAD"), cancellable = true)
    private void apotheosisSelfAffix$safeDamageReductionDescription(ItemStack stack, LootRarity rarity, float level, CallbackInfoReturnable<MutableComponent> cir) {
        if (this.values != null && !this.values.containsKey(rarity)) {
            LootRarity effective = SoulAffixHelper.getEffectiveRarity((DamageReductionAffix)(Object)this, rarity);
            if (effective != null && effective != rarity && this.values.containsKey(effective)) {
                try {
                    cir.setReturnValue(((DamageReductionAffix)(Object)this).getDescription(stack, effective, level));
                } catch (Exception ignored) {
                }
            } else {
                cir.setReturnValue(Component.empty());
            }
        }
    }

    @Inject(method = "getDamageProtection", at = @At("HEAD"), cancellable = true)
    private void apotheosisSelfAffix$safeDamageProtection(ItemStack stack, LootRarity rarity, float level, DamageSource source, CallbackInfoReturnable<Integer> cir) {
        if (this.values != null && !this.values.containsKey(rarity)) {
            LootRarity effective = SoulAffixHelper.getEffectiveRarity((DamageReductionAffix)(Object)this, rarity);
            if (effective != null && effective != rarity && this.values.containsKey(effective)) {
                try {
                    cir.setReturnValue(((DamageReductionAffix)(Object)this).getDamageProtection(stack, effective, level, source));
                } catch (Exception ignored) {
                }
            } else {
                cir.setReturnValue(0);
            }
        }
    }
}
