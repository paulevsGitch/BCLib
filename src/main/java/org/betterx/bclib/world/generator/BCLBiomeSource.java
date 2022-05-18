package org.betterx.bclib.world.generator;

import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeSource;

import org.betterx.bclib.api.biomes.BiomeAPI;

import java.util.List;

public abstract class BCLBiomeSource extends BiomeSource {
    protected final Registry<Biome> biomeRegistry;
    protected long currentSeed;

    private static List<Holder<Biome>> preInit(Registry<Biome> biomeRegistry, List<Holder<Biome>> biomes) {
        biomes.forEach(biome -> BiomeAPI.sortBiomeFeatures(biome));
        return biomes;
    }

    protected BCLBiomeSource(Registry<Biome> biomeRegistry, List<Holder<Biome>> list) {
        super(preInit(biomeRegistry, list));
        System.out.println(this + " with Registry: " + biomeRegistry.getClass().getName() + "@" + Integer.toHexString(
                biomeRegistry.hashCode()));
        this.biomeRegistry = biomeRegistry;

        BiomeAPI.initRegistry(biomeRegistry);
    }

    public void setSeed(long seed) {
        System.out.println(this + " set Seed: " + seed);
        this.currentSeed = seed;
    }
}
