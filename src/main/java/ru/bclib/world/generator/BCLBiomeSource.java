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
		String group = bclBiome.configGroup();
		float chance = Configs.BIOMES_CONFIG.getFloat(group, "generation_chance", bclBiome.getGenChance());
		float fog = Configs.BIOMES_CONFIG.getFloat(group, "fog_density", bclBiome.getFogDensity());
		bclBiome.setGenChance(chance)
				.setFogDensity(fog);
		
		if (bclBiome.getEdge()!=null){
			int edgeSize = Configs.BIOMES_CONFIG.getInt(group, "edge_size", bclBiome.getEdgeSize());
			bclBiome.setEdgeSize(edgeSize);
		}
		
		Configs.BIOMES_CONFIG.saveChanges();
		
		return bclBiome;
	}
}
