package com.yutaka.apotheosis_self_affix.mixin;

import com.yutaka.apotheosis_self_affix.item.SoulItem;
import dev.shadowsoffire.apotheosis.adventure.affix.Affix;
import dev.shadowsoffire.apotheosis.adventure.affix.AffixHelper;
import dev.shadowsoffire.apotheosis.adventure.affix.AffixInstance;
import dev.shadowsoffire.apotheosis.adventure.affix.AffixRegistry;
import dev.shadowsoffire.apotheosis.adventure.loot.LootRarity;
import dev.shadowsoffire.apotheosis.adventure.loot.RarityRegistry;
import dev.shadowsoffire.placebo.reload.DynamicHolder;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

@Mixin(value = AffixHelper.class, remap = false)
public class AffixHelperMixin {

    @Inject(method = "getAffixesImpl", at = @At("HEAD"), cancellable = true, remap = false)
    private static void getAffixesImplForSoul(ItemStack stack, CallbackInfoReturnable<Map<DynamicHolder<? extends Affix>, AffixInstance>> cir) {
        if (stack.getItem() instanceof SoulItem) {
            if (stack.isEmpty()) {
                cir.setReturnValue(Collections.emptyMap());
                return;
            }

            CompoundTag afxData = stack.getTagElement(AffixHelper.AFFIX_DATA);
            if (afxData == null || !afxData.contains(AffixHelper.AFFIXES)) {
                cir.setReturnValue(Collections.emptyMap());
                return;
            }

            CompoundTag affixesTag = afxData.getCompound(AffixHelper.AFFIXES);
            DynamicHolder<LootRarity> rarityHolder = AffixHelper.getRarity(afxData);
            if (!rarityHolder.isBound()) {
                rarityHolder = RarityRegistry.getMinRarity();
            }

            Map<DynamicHolder<? extends Affix>, AffixInstance> map = new HashMap<>();
            for (String key : affixesTag.getAllKeys()) {
                DynamicHolder<Affix> holder = AffixRegistry.INSTANCE.holder(new ResourceLocation(key));
                if (holder.isBound()) {
                    float level = affixesTag.getFloat(key);
                    map.put(holder, new AffixInstance(holder, stack, rarityHolder, level));
                }
            }
            cir.setReturnValue(map);
        }
    }
}
