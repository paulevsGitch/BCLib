package org.betterx.bclib.mixin.common;

import net.minecraft.server.dedicated.DedicatedServerProperties;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(DedicatedServerProperties.WorldGenProperties.class)
public class WorldGenPropertiesMixin {
    @ModifyArg(method = "create", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/levelgen/presets/WorldPreset;createWorldGenSettings(JZZ)Lnet/minecraft/world/level/levelgen/WorldGenSettings;"))
    public long bcl_create(long seed) {
        return seed;
    }
}