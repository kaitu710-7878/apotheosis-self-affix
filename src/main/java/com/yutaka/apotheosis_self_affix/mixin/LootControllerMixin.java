package com.yutaka.apotheosis_self_affix.mixin;

import com.yutaka.apotheosis_self_affix.item.SoulItem;
import com.yutaka.apotheosis_self_affix.util.SoulAffixHelper;
import dev.shadowsoffire.apotheosis.adventure.affix.Affix;
import dev.shadowsoffire.apotheosis.adventure.affix.AffixHelper;
import dev.shadowsoffire.apotheosis.adventure.affix.AffixType;
import dev.shadowsoffire.apotheosis.adventure.loot.LootController;
import dev.shadowsoffire.apotheosis.adventure.loot.LootRarity;
import dev.shadowsoffire.placebo.reload.DynamicHolder;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Mixin(value = LootController.class, remap = false)
public class LootControllerMixin {

    @Inject(method = "getAvailableAffixes", at = @At("HEAD"), cancellable = true, remap = false)
    private static void getAvailableAffixesForSoul(
            ItemStack stack,
            LootRarity rarity,
            Set<DynamicHolder<? extends Affix>> currentAffixes,
            AffixType type,
            CallbackInfoReturnable<List<DynamicHolder<? extends Affix>>> cir
    ) {
        if (stack.getItem() instanceof SoulItem) {
            @SuppressWarnings({"unchecked", "rawtypes"})
            List<DynamicHolder<? extends Affix>> available = new java.util.ArrayList<>(
                    (List) AffixHelper.byType(type).stream()
                            .filter(holder -> {
                                Affix affix = holder.get();
                                return affix != null && SoulAffixHelper.canAffixApplyToSoul(affix, stack, rarity);
                            })
                            .filter(holder -> currentAffixes == null || !currentAffixes.contains(holder))
                            .toList()
            );
            cir.setReturnValue(available);
        }
    }
}
