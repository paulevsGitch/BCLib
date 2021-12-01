package ru.bclib.api.biomes;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.biome.Biome.BiomeBuilder;
import net.minecraft.world.level.biome.Biome.BiomeCategory;
import net.minecraft.world.level.biome.Biome.Precipitation;
import net.minecraft.world.level.biome.BiomeSpecialEffects;
import net.minecraft.world.level.biome.MobSpawnSettings;
import net.minecraft.world.level.biome.MobSpawnSettings.SpawnerData;
import ru.bclib.world.biomes.BCLBiome;

import java.util.ArrayList;
import java.util.List;

public class BCLBiomeBuilder {
	private static final BCLBiomeBuilder INSTANCE = new BCLBiomeBuilder();
	
	private List<SpawnerData> mobs = new ArrayList<>(32);
	private BiomeSpecialEffects.Builder effectsBuilder;
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
		INSTANCE.effectsBuilder = null;
		INSTANCE.temperature = 1.0F;
		INSTANCE.mobs.clear();
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
	
	/**
	 * Adds mob spawning to biome.
	 * @param entityType {@link EntityType} mob type.
	 * @param weight spawn weight.
	 * @param minGroupCount minimum mobs in group.
	 * @param maxGroupCount maximum mobs in group.
	 * @return
	 */
	public <M extends Mob> BCLBiomeBuilder spawn(EntityType<M> entityType, int weight, int minGroupCount, int maxGroupCount) {
		mobs.add(new SpawnerData(entityType, weight, minGroupCount, maxGroupCount));
		return this;
	}
	
	public BCLBiome build() {
		BiomeBuilder builder = new BiomeBuilder()
			.precipitation(precipitation)
			.biomeCategory(category)
			.temperature(temperature)
			.downfall(downfall);
			/*
			.generationSettings(generationSettings.build())*/
			//.build();
		
		if (!mobs.isEmpty()) {
			MobSpawnSettings.Builder spawnSettings = new MobSpawnSettings.Builder();
			mobs.forEach(spawn -> spawnSettings.addSpawn(spawn.type.getCategory(), spawn));
			builder.mobSpawnSettings(spawnSettings.build());
		}
		
		if (effectsBuilder != null) {
			builder.specialEffects(effectsBuilder.build());
		}
		
		return new BCLBiome(biomeID, builder.build());
	}
	
	private BiomeSpecialEffects.Builder getEffects() {
		if (effectsBuilder == null) {
			effectsBuilder = new BiomeSpecialEffects.Builder();
		}
		return effectsBuilder;
	}
}
