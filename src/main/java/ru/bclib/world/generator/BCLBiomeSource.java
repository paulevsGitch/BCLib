package ru.bclib.world.generator;

import net.minecraft.core.Registry;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeSource;
import ru.bclib.api.biomes.BiomeAPI;
import ru.bclib.config.Configs;
import ru.bclib.world.biomes.BCLBiome;

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
	
	/**
	 * Set Biome configuartion from Config
	 * @param bclBiome The biome you want to configure
	 * @return The input biome
	 */
	public static BCLBiome setupFromConfig(BCLBiome bclBiome) {
		bclBiome.setupFromConfig();
		return bclBiome;
	}
}
