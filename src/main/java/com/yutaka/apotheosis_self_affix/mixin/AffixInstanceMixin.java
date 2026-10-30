package com.yutaka.apotheosis_self_affix.mixin;

import com.yutaka.apotheosis_self_affix.util.SoulAffixHelper;
import dev.shadowsoffire.apotheosis.adventure.affix.Affix;
import dev.shadowsoffire.apotheosis.adventure.affix.AffixInstance;
import dev.shadowsoffire.apotheosis.adventure.loot.LootRarity;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = AffixInstance.class, remap = false)
public abstract class AffixInstanceMixin {

    @Shadow public abstract Affix afx();
    @Shadow public abstract LootRarity rty();
    @Shadow public abstract ItemStack stack();
    @Shadow public abstract float level();

    @Inject(method = "getDescription()Lnet/minecraft/network/chat/MutableComponent;", at = @At("HEAD"), cancellable = true)
    private void apotheosisSelfAffix$safeGetDescription(CallbackInfoReturnable<MutableComponent> cir) {
        Affix affix = afx();
        LootRarity original = rty();
        if (affix != null && original != null) {
            LootRarity effective = SoulAffixHelper.getEffectiveRarity(affix, original);
            if (effective != null && effective != original) {
                try {
                    MutableComponent desc = affix.getDescription(stack(), effective, level());
                    if (desc != null) {
                        cir.setReturnValue(desc);
                    }
                } catch (Exception ignored) {
                }
            }
        }
    }

    @Inject(method = "getAugmentingText()Lnet/minecraft/network/chat/Component;", at = @At("HEAD"), cancellable = true)
    private void apotheosisSelfAffix$safeGetAugmentingText(CallbackInfoReturnable<Component> cir) {
        Affix affix = afx();
        LootRarity original = rty();
        if (affix != null && original != null) {
            LootRarity effective = SoulAffixHelper.getEffectiveRarity(affix, original);
            if (effective != null && effective != original) {
                try {
                    Component text = affix.getAugmentingText(stack(), effective, level());
                    if (text != null) {
                        cir.setReturnValue(text);
                    }
                } catch (Exception ignored) {
                }
            }
        }
    }
}
