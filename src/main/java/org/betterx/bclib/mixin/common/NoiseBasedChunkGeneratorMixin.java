package org.betterx.bclib.mixin.common;

import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.levelgen.*;
import net.minecraft.world.level.levelgen.blending.Blender;
import net.minecraft.world.level.levelgen.synth.NormalNoise;

import org.betterx.bclib.interfaces.NoiseGeneratorSettingsProvider;
import org.betterx.bclib.interfaces.SurfaceProvider;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import java.lang.reflect.Constructor;

@Mixin(NoiseBasedChunkGenerator.class)
public abstract class NoiseBasedChunkGeneratorMixin implements SurfaceProvider, NoiseGeneratorSettingsProvider {
    @Final
    @Shadow
    protected Holder<NoiseGeneratorSettings> settings;

    @Shadow
    @Final
    private Registry<NormalNoise.NoiseParameters> noises;
    @Final
    @Shadow
    private Aquifer.FluidPicker globalFluidPicker;

    private static final BlockState bclib_air = Blocks.AIR.defaultBlockState();
    private static Constructor<?> bclib_constructor;

    @Override
    public NoiseGeneratorSettings bclib_getNoiseGeneratorSettings() {
        return settings.value();
    }

    @Override
    public Registry<NormalNoise.NoiseParameters> bclib_getNoises() {
        return noises;
    }

    @Shadow
    protected abstract NoiseChunk createNoiseChunk(ChunkAccess chunkAccess,
                                                   StructureManager structureManager,
                                                   Blender blender,
                                                   RandomState randomState);

}
