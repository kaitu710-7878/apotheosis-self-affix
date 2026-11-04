package com.yutaka.apotheosis_self_affix.mixin;

import com.yutaka.apotheosis_self_affix.item.SoulItem;
import dev.shadowsoffire.apotheosis.adventure.socket.SocketHelper;
import dev.shadowsoffire.apotheosis.adventure.socket.SocketedGems;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = SocketHelper.class, remap = false)
public class SocketHelperMixin {

    @Inject(method = "hasEmptySockets", at = @At("HEAD"), cancellable = true)
    private static void apotheosisSelfAffix$noEmptySockets(ItemStack stack, CallbackInfoReturnable<Boolean> cir) {
        if (stack.getItem() instanceof SoulItem) {
            cir.setReturnValue(false);
        }
    }

    @Inject(method = "getGems", at = @At("HEAD"), cancellable = true)
    private static void apotheosisSelfAffix$noGemsForSoul(ItemStack stack, CallbackInfoReturnable<SocketedGems> cir) {
        if (stack.getItem() instanceof SoulItem) {
            cir.setReturnValue(SocketedGems.EMPTY);
        }
    }

    @Inject(method = "setGems", at = @At("HEAD"), cancellable = true)
    private static void apotheosisSelfAffix$preventSetGems(ItemStack stack, SocketedGems gems, CallbackInfo ci) {
        if (stack.getItem() instanceof SoulItem) {
            CompoundTag tag = stack.getTagElement(SocketHelper.AFFIX_DATA);
            if (tag != null) {
                tag.remove(SocketHelper.GEMS);
            }
            ci.cancel();
        }
    }

    @Inject(method = "getFirstEmptySocket", at = @At("HEAD"), cancellable = true)
    private static void apotheosisSelfAffix$noFirstEmptySocket(ItemStack stack, CallbackInfoReturnable<Integer> cir) {
        if (stack.getItem() instanceof SoulItem) {
            cir.setReturnValue(0);
        }
    }
}
