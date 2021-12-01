package ru.bclib.world.biomes;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.biome.Biome;

public class BCLBiomeBuilder {
	private static final BCLBiomeBuilder INSTANCE = new BCLBiomeBuilder();
	
	private ResourceLocation biomeID;
	
	public static BCLBiomeBuilder start(ResourceLocation biomeID) {
		INSTANCE.biomeID = biomeID;
		return INSTANCE;
	}
	
	public BCLBiome build() {
		Biome biome = new Biome.BiomeBuilder()
			/*.precipitation(precipitation)
			.biomeCategory(category)
			.temperature(temperature)
			.downfall(downfall)
			.specialEffects(effects.build())
			.mobSpawnSettings(spawnSettings.build())
			.generationSettings(generationSettings.build())*/
			.build();
		return new BCLBiome(biomeID, biome);
	}
}
