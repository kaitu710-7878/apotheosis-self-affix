package com.yutaka.apotheosis_self_affix.mixin;

import com.yutaka.apotheosis_self_affix.item.SoulItem;
import dev.shadowsoffire.apotheosis.adventure.loot.LootRarity;
import dev.shadowsoffire.apotheosis.adventure.socket.gem.Gem;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = Gem.class, remap = false)
public class GemMixin {

    @Inject(method = "canApplyTo(Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/item/ItemStack;Ldev/shadowsoffire/apotheosis/adventure/loot/LootRarity;)Z", at = @At("HEAD"), cancellable = true)
    private void apotheosisSelfAffix$denyGemCanApplyTo(ItemStack stack, ItemStack gem, LootRarity rarity, CallbackInfoReturnable<Boolean> cir) {
        if (stack.getItem() instanceof SoulItem) {
            cir.setReturnValue(false);
        }
    }

    @Inject(method = "isValidIn", at = @At("HEAD"), cancellable = true)
    private void apotheosisSelfAffix$denyGemIsValidIn(ItemStack stack, ItemStack gem, LootRarity rarity, CallbackInfoReturnable<Boolean> cir) {
        if (stack.getItem() instanceof SoulItem) {
            cir.setReturnValue(false);
        }
    }
}
