package com.yutaka.apotheosis_self_affix.mixin;

import com.yutaka.apotheosis_self_affix.item.SoulItem;
import dev.shadowsoffire.apotheosis.adventure.socket.SocketingRecipe;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = SocketingRecipe.class)
public class SocketingRecipeMixin {

    @Inject(method = "matches(Lnet/minecraft/world/Container;Lnet/minecraft/world/level/Level;)Z", at = @At("HEAD"), cancellable = true)
    private void apotheosisSelfAffix$denySocketingToSoul(Container inv, Level level, CallbackInfoReturnable<Boolean> cir) {
        ItemStack base = inv.getItem(1);
        if (base.getItem() instanceof SoulItem) {
            cir.setReturnValue(false);
        }
    }
}
