package com.yutaka.apotheosis_self_affix.capability;

import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.CapabilityToken;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import net.minecraftforge.common.util.LazyOptional;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class SoulCapabilityProvider implements ICapabilitySerializable<CompoundTag> {

    public static final ResourceLocation IDENTIFIER = new ResourceLocation("apotheosis_self_affix", "player_soul");
    public static final Capability<ISoulHandler> SOUL_CAPABILITY = CapabilityManager.get(new CapabilityToken<>() {});

    private final ISoulHandler soulHandler;
    private final LazyOptional<ISoulHandler> optional;

    public SoulCapabilityProvider(Runnable onChange) {
        this.soulHandler = new SoulHandler(onChange);
        this.optional = LazyOptional.of(() -> soulHandler);
    }

    @Override
    public @NotNull <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
        if (cap == SOUL_CAPABILITY) {
            return optional.cast();
        }
        return LazyOptional.empty();
    }

    @Override
    public CompoundTag serializeNBT() {
        return soulHandler.serializeNBT();
    }

    @Override
    public void deserializeNBT(CompoundTag nbt) {
        soulHandler.deserializeNBT(nbt);
    }
}
