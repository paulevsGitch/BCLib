package org.betterx.bclib.world.generator;

import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeSource;

import org.betterx.bclib.api.biomes.BiomeAPI;
import org.betterx.bclib.presets.worldgen.BCLChunkGenerator;

import java.util.List;
import java.util.Optional;

public abstract class BCLBiomeSource extends BiomeSource {
    protected final Registry<Biome> biomeRegistry;
    protected long currentSeed;

    public final int biomeSourceVersion;

    private static List<Holder<Biome>> preInit(Registry<Biome> biomeRegistry, List<Holder<Biome>> biomes) {
        biomes.forEach(biome -> BiomeAPI.sortBiomeFeatures(biome));
        return biomes;
    }

    protected BCLBiomeSource(Registry<Biome> biomeRegistry,
                             List<Holder<Biome>> list,
                             long seed,
                             Optional<Integer> biomeSourceVersion) {
        super(preInit(biomeRegistry, list));

        this.biomeRegistry = biomeRegistry;
        this.biomeSourceVersion = biomeSourceVersion.orElse(BCLChunkGenerator.DEFAULT_BIOME_SOURCE_VERSION);
        this.currentSeed = seed;

        System.out.println(this + " with Registry: " + biomeRegistry.getClass().getName() + "@" + Integer.toHexString(
                biomeRegistry.hashCode()));

        BiomeAPI.initRegistry(biomeRegistry);
    }

    final public void setSeed(long seed) {
        if (seed != currentSeed) {
            System.out.println(this + " set Seed: " + seed);
            this.currentSeed = seed;
            initMap(seed);
        }
    }

    protected final void initMap(long seed) {
        System.out.println(this + " updates Map");
        onInitMap(seed);
    }

    protected abstract void onInitMap(long newSeed);
}
