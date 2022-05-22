package org.betterx.bclib.mixin.common;

import net.minecraft.core.Registry;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.chunk.ChunkGenerators;

import com.mojang.serialization.Codec;
import org.betterx.bclib.BCLib;
import org.betterx.bclib.presets.worldgen.BCLChunkGenerator;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ChunkGenerators.class)
public class ChunkGeneratorsMixin {
    @Inject(method = "bootstrap", at = @At(value = "HEAD"))
    private static void bcl_bootstrap(Registry<Codec<? extends ChunkGenerator>> registry,
                                      CallbackInfoReturnable<Codec<? extends ChunkGenerator>> cir) {
        Registry.register(registry, BCLib.makeID("betterx"), BCLChunkGenerator.CODEC);
    }
}
