package com.yutaka.apotheosis_self_affix.mixin;

import com.yutaka.apotheosis_self_affix.util.SoulAffixHelper;
import dev.shadowsoffire.apotheosis.adventure.affix.Affix;
import dev.shadowsoffire.apotheosis.adventure.affix.AffixInstance;
import dev.shadowsoffire.apotheosis.adventure.loot.LootRarity;
import dev.shadowsoffire.apotheosis.adventure.loot.RarityRegistry;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.ArrayList;
import java.util.List;

@Mixin(value = AffixInstance.class, remap = false, priority = 500)
public abstract class AffixInstanceMixin {

    @Shadow public abstract Affix afx();
    @Shadow public abstract LootRarity rty();
    @Shadow public abstract ItemStack stack();
    @Shadow public abstract float level();

    @Inject(method = "getDescription()Lnet/minecraft/network/chat/MutableComponent;", at = @At("HEAD"), cancellable = true)
    private void apotheosisSelfAffix$safeGetDescription(CallbackInfoReturnable<MutableComponent> cir) {
        Affix affix = afx();
        if (affix == null) {
            cir.setReturnValue(Component.empty());
            return;
        }

        LootRarity original = rty();
        LootRarity effective = SoulAffixHelper.getEffectiveRarity(affix, original);

        // 1. Try effective rarity first (bridges custom tiers like Esoteric/Ancient down to highest supported tier)
        if (effective != null) {
            try {
                MutableComponent desc = affix.getDescription(stack(), effective, level());
                if (desc != null) {
                    cir.setReturnValue(desc);
                    return;
                }
            } catch (Throwable ignored) {
            }
        }

        // 2. Try original rarity
        if (original != null && original != effective) {
            try {
                MutableComponent desc = affix.getDescription(stack(), original, level());
                if (desc != null) {
                    cir.setReturnValue(desc);
                    return;
                }
            } catch (Throwable ignored) {
            }
        }

        // 3. Fallback: try all registered rarities in descending order (Mythic -> Common)
        List<LootRarity> rarities = new ArrayList<>(RarityRegistry.INSTANCE.getValues());
        rarities.sort((a, b) -> Integer.compare(b.ordinal(), a.ordinal()));
        for (LootRarity r : rarities) {
            try {
                MutableComponent desc = affix.getDescription(stack(), r, level());
                if (desc != null) {
                    cir.setReturnValue(desc);
                    return;
                }
            } catch (Throwable ignored) {
            }
        }

        // 4. Fail-safe: never crash the tooltip screen!
        cir.setReturnValue(Component.empty());
    }

    @Inject(method = "getAugmentingText()Lnet/minecraft/network/chat/Component;", at = @At("HEAD"), cancellable = true)
    private void apotheosisSelfAffix$safeGetAugmentingText(CallbackInfoReturnable<Component> cir) {
        Affix affix = afx();
        if (affix == null) {
            cir.setReturnValue(Component.empty());
            return;
        }

        LootRarity original = rty();
        LootRarity effective = SoulAffixHelper.getEffectiveRarity(affix, original);

        if (effective != null) {
            try {
                Component text = affix.getAugmentingText(stack(), effective, level());
                if (text != null) {
                    cir.setReturnValue(text);
                    return;
                }
            } catch (Throwable ignored) {
            }
        }

        List<LootRarity> rarities = new ArrayList<>(RarityRegistry.INSTANCE.getValues());
        rarities.sort((a, b) -> Integer.compare(b.ordinal(), a.ordinal()));
        for (LootRarity r : rarities) {
            try {
                Component text = affix.getAugmentingText(stack(), r, level());
                if (text != null) {
                    cir.setReturnValue(text);
                    return;
                }
            } catch (Throwable ignored) {
            }
        }

        cir.setReturnValue(Component.empty());
    }
}
