package com.yutaka.apotheosis_self_affix.mixin;

import com.yutaka.apotheosis_self_affix.item.SoulItem;
import dev.shadowsoffire.apotheosis.adventure.affix.AttributeAffix;
import dev.shadowsoffire.apotheosis.adventure.loot.LootRarity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Map;
import java.util.function.BiConsumer;

@Mixin(value = AttributeAffix.class, remap = false)
public class AttributeAffixMixin {

    @Shadow
    protected transient Map<LootRarity, ?> modifiers;

    @Inject(method = "addModifiers", at = @At("HEAD"), cancellable = true)
    private void apotheosisSelfAffix$safeAddModifiers(ItemStack stack, LootRarity rarity, float level, EquipmentSlot slot, BiConsumer<Attribute, AttributeModifier> map, CallbackInfo ci) {
        // 1. If the item is a SoulItem, its attributes are managed independently by SoulAttributeManager
        // and SoulAffixHelper. Canceling here completely prevents crashes in vanilla/modded attribute modifier events.
        if (stack.getItem() instanceof SoulItem) {
            ci.cancel();
            return;
        }

        // 2. Safety guard: if the rarity is not in the internal modifiers map (e.g. Esoteric rarity tier
        // from esotericreforging), cancel to prevent NullPointerException: Cannot read field "attr" because "modif" is null.
        if (this.modifiers != null && !this.modifiers.containsKey(rarity)) {
            ci.cancel();
        }
    }
}
