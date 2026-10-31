package com.yutaka.apotheosis_self_affix.mixin;

import com.yutaka.apotheosis_self_affix.util.SoulAffixHelper;
import dev.shadowsoffire.apotheosis.adventure.affix.effect.DamageReductionAffix;
import dev.shadowsoffire.apotheosis.adventure.loot.LootRarity;
import dev.shadowsoffire.placebo.util.StepFunction;
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

    @Inject(method = "getTrueLevel", at = @At("HEAD"), cancellable = true)
    private void apotheosisSelfAffix$safeGetTrueLevel(LootRarity rarity, float level, CallbackInfoReturnable<Float> cir) {
        if (this.values != null && !this.values.containsKey(rarity)) {
            LootRarity effective = SoulAffixHelper.getEffectiveRarity((DamageReductionAffix)(Object)this, rarity);
            if (effective != null && this.values.containsKey(effective)) {
                StepFunction fn = this.values.get(effective);
                if (fn != null) {
                    cir.setReturnValue(fn.get(level));
                    return;
                }
            }
            cir.setReturnValue(0.0F);
        }
    }
}
