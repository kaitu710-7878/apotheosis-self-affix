package com.yutaka.apotheosis_self_affix.capability;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.items.IItemHandlerModifiable;

public interface ISoulHandler extends INBTSerializable<CompoundTag> {
    ItemStack getSoul();
    void setSoul(ItemStack soul);
    boolean hasSoul();
    IItemHandlerModifiable getItemHandler();
}
