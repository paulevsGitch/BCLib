package ru.bclib.mixin.common;

import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.levelgen.presets.WorldPreset;
import net.minecraft.world.level.levelgen.presets.WorldPresets;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(WorldPresets.class)
public interface WorldPresetsAccessor {
    @Invoker("register")
    public static ResourceKey<WorldPreset> callRegister(String string) {
        throw new RuntimeException("Not Implemented");
    }
}