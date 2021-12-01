package ru.bclib.world.biomes;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Biome.BiomeCategory;
import net.minecraft.world.level.biome.Biome.Precipitation;

public class BCLBiomeBuilder {
	private static final BCLBiomeBuilder INSTANCE = new BCLBiomeBuilder();
	
	private Precipitation precipitation;
	private ResourceLocation biomeID;
	private BiomeCategory category;
	private float temperature;
	private float downfall;
	
	/**
	 * Starts new biome building process.
	 * @param biomeID {@link ResourceLocation} biome identifier.
	 * @return prepared {@link BCLBiomeBuilder} instance.
	 */
	public static BCLBiomeBuilder start(ResourceLocation biomeID) {
		INSTANCE.biomeID = biomeID;
		INSTANCE.precipitation = Precipitation.NONE;
		INSTANCE.category = BiomeCategory.NONE;
		INSTANCE.temperature = 1.0F;
		return INSTANCE;
	}
	
	/**
	 * Set biome {@link Precipitation}. Affect biome visual effects (rain, snow, none).
	 * @param precipitation {@link Precipitation}
	 * @return same {@link BCLBiomeBuilder} instance.
	 */
	public BCLBiomeBuilder precipitation(Precipitation precipitation) {
		this.precipitation = precipitation;
		return this;
	}
	
	/**
	 * Set biome category. Doesn't affect biome worldgen, but Fabric biome modifications can target biome by it.
	 * @param category {@link BiomeCategory}
	 * @return same {@link BCLBiomeBuilder} instance.
	 */
	public BCLBiomeBuilder category(BiomeCategory category) {
		this.category = category;
		return this;
	}
	
	/**
	 * Set biome temperature, affect plant color, biome generation and ice formation.
	 * @param temperature biome temperature.
	 * @return same {@link BCLBiomeBuilder} instance.
	 */
	public BCLBiomeBuilder temperature(float temperature) {
		this.temperature = temperature;
		return this;
	}
	
	/**
	 * Set biome wetness (same as downfall). Affect plant color and biome generation.
	 * @param wetness biome wetness (downfall).
	 * @return same {@link BCLBiomeBuilder} instance.
	 */
	public BCLBiomeBuilder wetness(float wetness) {
		this.downfall = wetness;
		return this;
	}
	
	public BCLBiome build() {
		Biome biome = new Biome.BiomeBuilder()
			.precipitation(precipitation)
			.biomeCategory(category)
			.temperature(temperature)
			.downfall(downfall)
			/*
			.specialEffects(effects.build())
			.mobSpawnSettings(spawnSettings.build())
			.generationSettings(generationSettings.build())*/
			.build();
		return new BCLBiome(biomeID, biome);
	}
}
