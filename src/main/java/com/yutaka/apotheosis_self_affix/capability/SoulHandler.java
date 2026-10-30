package com.yutaka.apotheosis_self_affix.capability;

import com.yutaka.apotheosis_self_affix.item.SoulItem;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.IItemHandlerModifiable;
import net.minecraftforge.items.ItemStackHandler;
import org.jetbrains.annotations.NotNull;

public class SoulHandler implements ISoulHandler {

    private final Runnable onChangeCallback;

    private final ItemStackHandler itemHandler = new ItemStackHandler(1) {
        @Override
        public boolean isItemValid(int slot, @NotNull ItemStack stack) {
            return stack.getItem() instanceof SoulItem;
        }

        @Override
        public int getSlotLimit(int slot) {
            return 1;
        }

        @Override
        protected void onContentsChanged(int slot) {
            super.onContentsChanged(slot);
            if (onChangeCallback != null) {
                onChangeCallback.run();
            }
        }
    };

    public SoulHandler(Runnable onChangeCallback) {
        this.onChangeCallback = onChangeCallback;
    }

    public SoulHandler() {
        this(null);
    }

    @Override
    public ItemStack getSoul() {
        return itemHandler.getStackInSlot(0);
    }

    @Override
    public void setSoul(ItemStack soul) {
        itemHandler.setStackInSlot(0, soul);
    }

    @Override
    public boolean hasSoul() {
        return !getSoul().isEmpty();
    }

    @Override
    public IItemHandlerModifiable getItemHandler() {
        return itemHandler;
    }

    @Override
    public CompoundTag serializeNBT() {
        return itemHandler.serializeNBT();
    }

    @Override
    public void deserializeNBT(CompoundTag nbt) {
        itemHandler.deserializeNBT(nbt);
    }
}
