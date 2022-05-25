package org.betterx.bclib.api.worldgen;

import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.resources.RegistryOps;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.NoiseBasedChunkGenerator;
import net.minecraft.world.level.levelgen.NoiseGeneratorSettings;
import net.minecraft.world.level.levelgen.structure.StructureSet;
import net.minecraft.world.level.levelgen.synth.NormalNoise;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import org.betterx.bclib.interfaces.NoiseGeneratorSettingsProvider;

public class BCLChunkGenerator extends NoiseBasedChunkGenerator {

    public static final Codec<BCLChunkGenerator> CODEC = RecordCodecBuilder
            .create((RecordCodecBuilder.Instance<BCLChunkGenerator> builderInstance) -> {
                final RecordCodecBuilder<BCLChunkGenerator, Registry<NormalNoise.NoiseParameters>> noiseGetter = RegistryOps
                        .retrieveRegistry(
                                Registry.NOISE_REGISTRY)
                        .forGetter(
                                BCLChunkGenerator::getNoises);

                RecordCodecBuilder<BCLChunkGenerator, BiomeSource> biomeSourceCodec = BiomeSource.CODEC
                        .fieldOf("biome_source")
                        .forGetter((BCLChunkGenerator generator) -> generator.biomeSource);

                RecordCodecBuilder<BCLChunkGenerator, Holder<NoiseGeneratorSettings>> settingsCodec = NoiseGeneratorSettings.CODEC
                        .fieldOf("settings")
                        .forGetter((BCLChunkGenerator generator) -> generator.settings);


                return NoiseBasedChunkGenerator
                        .commonCodec(builderInstance)
                        .and(builderInstance.group(noiseGetter, biomeSourceCodec, settingsCodec))
                        .apply(builderInstance, builderInstance.stable(BCLChunkGenerator::new));
            });

    public BCLChunkGenerator(Registry<StructureSet> registry,
                             Registry<NormalNoise.NoiseParameters> registry2,
                             BiomeSource biomeSource,
                             Holder<NoiseGeneratorSettings> holder) {
        super(registry, registry2, biomeSource, holder);
        System.out.println("Chunk Generator: " + this);
    }

    @Override
    protected Codec<? extends ChunkGenerator> codec() {
        return CODEC;
    }


    private Registry<NormalNoise.NoiseParameters> getNoises() {
        if (this instanceof NoiseGeneratorSettingsProvider p) {
            return p.bclib_getNoises();
        }
        return null;
    }

    @Override
    public String toString() {
        return "BCLib - Chunk Generator (" + Integer.toHexString(hashCode()) + ")";
    }
}
