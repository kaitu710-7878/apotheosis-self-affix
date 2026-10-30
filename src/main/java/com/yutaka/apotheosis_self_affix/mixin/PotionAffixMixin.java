package com.yutaka.apotheosis_self_affix.mixin;

import com.yutaka.apotheosis_self_affix.util.SoulAffixHelper;
import dev.shadowsoffire.apotheosis.adventure.affix.effect.PotionAffix;
import dev.shadowsoffire.apotheosis.adventure.loot.LootRarity;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Map;

@Mixin(value = PotionAffix.class, remap = false)
public class PotionAffixMixin {

    @Shadow
    protected Map<LootRarity, ?> values;

    @Inject(method = "getDescription", at = @At("HEAD"), cancellable = true)
    private void apotheosisSelfAffix$safePotionDescription(ItemStack stack, LootRarity rarity, float level, CallbackInfoReturnable<MutableComponent> cir) {
        if (this.values != null && !this.values.containsKey(rarity)) {
            LootRarity effective = SoulAffixHelper.getEffectiveRarity((PotionAffix)(Object)this, rarity);
            if (effective != null && effective != rarity && this.values.containsKey(effective)) {
                try {
                    cir.setReturnValue(((PotionAffix)(Object)this).getDescription(stack, effective, level));
                } catch (Exception ignored) {
                }
            } else {
                cir.setReturnValue(Component.empty());
            }
        }
    }

    @Inject(method = "getAugmentingText", at = @At("HEAD"), cancellable = true)
    private void apotheosisSelfAffix$safePotionAugmentingText(ItemStack stack, LootRarity rarity, float level, CallbackInfoReturnable<Component> cir) {
        if (this.values != null && !this.values.containsKey(rarity)) {
            LootRarity effective = SoulAffixHelper.getEffectiveRarity((PotionAffix)(Object)this, rarity);
            if (effective != null && effective != rarity && this.values.containsKey(effective)) {
                try {
                    cir.setReturnValue(((PotionAffix)(Object)this).getAugmentingText(stack, effective, level));
                } catch (Exception ignored) {
                }
            } else {
                cir.setReturnValue(Component.empty());
            }
        }
    }
}
