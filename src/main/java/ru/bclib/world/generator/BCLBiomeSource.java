package ru.bclib.world.generator;

import net.minecraft.core.Registry;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeSource;
import ru.bclib.api.biomes.BiomeAPI;

import java.util.List;

public abstract class BCLBiomeSource extends BiomeSource {
	protected final Registry<Biome> biomeRegistry;
	protected final long seed;

	private static List<Biome> preInit(Registry<Biome> biomeRegistry, List<Biome> biomes){
		biomes.forEach(biome -> BiomeAPI.sortBiomeFeatures(biome));
		return biomes;
	}

	protected BCLBiomeSource(Registry<Biome> biomeRegistry, long seed, List<Biome> list) {
		super(preInit(biomeRegistry, list));

		this.seed = seed;
		this.biomeRegistry = biomeRegistry;

		BiomeAPI.initRegistry(biomeRegistry);
	}
}
