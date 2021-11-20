package ru.bclib.world.biomes;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import net.minecraft.core.Registry;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.Music;
import net.minecraft.sounds.Musics;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.biome.AmbientAdditionsSettings;
import net.minecraft.world.level.biome.AmbientMoodSettings;
import net.minecraft.world.level.biome.AmbientParticleSettings;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Biome.BiomeCategory;
import net.minecraft.world.level.biome.Biome.Precipitation;
import net.minecraft.world.level.biome.BiomeGenerationSettings;
import net.minecraft.world.level.biome.BiomeSpecialEffects.Builder;
import net.minecraft.world.level.biome.MobSpawnSettings;
import net.minecraft.world.level.biome.MobSpawnSettings.SpawnerData;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.GenerationStep.Carving;
import net.minecraft.world.level.levelgen.GenerationStep.Decoration;
import net.minecraft.world.level.levelgen.carver.CarverConfiguration;
import net.minecraft.world.level.levelgen.carver.ConfiguredWorldCarver;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.feature.ConfiguredStructureFeature;
import net.minecraft.world.level.levelgen.surfacebuilders.ConfiguredSurfaceBuilder;
import net.minecraft.world.level.levelgen.surfacebuilders.SurfaceBuilder;
import net.minecraft.world.level.levelgen.surfacebuilders.SurfaceBuilderBaseConfiguration;
import ru.bclib.config.IdConfig;
import ru.bclib.config.PathConfig;
import ru.bclib.util.ColorUtil;
import ru.bclib.world.features.BCLFeature;
import ru.bclib.world.structures.BCLStructureFeature;
import ru.bclib.world.surface.DoubleBlockSurfaceBuilder;

import java.util.List;
import java.util.Map;

public class BCLBiomeDef {
	public static final int DEF_FOLIAGE_OVERWORLD = ColorUtil.color(110, 143, 64);
	public static final int DEF_FOLIAGE_NETHER = ColorUtil.color(117, 10, 10);
	public static final int DEF_FOLIAGE_END = ColorUtil.color(197, 210, 112);
	
	private final List<ConfiguredStructureFeature<?, ?>> structures = Lists.newArrayList();
	private final List<FeatureInfo> features = Lists.newArrayList();
	private final List<CarverInfo> carvers = Lists.newArrayList();
	private final List<SpawnInfo> mobs = Lists.newArrayList();
	private final List<SpawnerData> spawns = Lists.newArrayList();
	private final Map<String, Object> customData = Maps.newHashMap();
	
	private final ResourceLocation id;
	
	private AmbientParticleSettings particleConfig;
	private AmbientAdditionsSettings additions;
	private AmbientMoodSettings mood;
	private SoundEvent music;
	private SoundEvent loop;
	
	private int foliageColor = DEF_FOLIAGE_OVERWORLD;
	private int grassColor = DEF_FOLIAGE_OVERWORLD;
	private int waterFogColor = 329011;
	private int waterColor = 4159204;
	private int fogColor = 10518688;
	private int skyColor = 0;
	private float fogDensity = 1F;
	private float depth = 0.1F;
	
	private Precipitation precipitation = Precipitation.NONE;
	private BiomeCategory category = BiomeCategory.NONE;
	private float temperature = 1F;
	private float genChance = 1F;
	private float downfall = 0F;
	private int edgeSize = 32;
	
	private ConfiguredSurfaceBuilder<?> surface;
	
	/**
	 * Custom biome definition. Can be extended with new parameters.
	 *
	 * @param id - Biome {@link ResourceLocation} (identifier).
	 */
	public BCLBiomeDef(ResourceLocation id) {
		this.id = id;
	}
	
	/**
	 * Create default definition for The Nether biome.
	 *
	 * @return {@link BCLBiomeDef}.
	 */
	public BCLBiomeDef netherBiome() {
		this.foliageColor = DEF_FOLIAGE_NETHER;
		this.grassColor = DEF_FOLIAGE_NETHER;
		this.setCategory(BiomeCategory.NETHER);
		return this;
	}
	
	/**
	 * Create default definition for The End biome.
	 *
	 * @return {@link BCLBiomeDef}.
	 */
	public BCLBiomeDef endBiome() {
		this.foliageColor = DEF_FOLIAGE_END;
		this.grassColor = DEF_FOLIAGE_END;
		this.setCategory(BiomeCategory.THEEND);
		return this;
	}
	
	/**
	 * Used to load biome settings from config.
	 * @param config - {@link IdConfig}.
	 * @return this {@link BCLBiomeDef}.
	 */
	public BCLBiomeDef loadConfigValues(IdConfig config) {
		this.fogDensity = config.getFloat(id, "fog_density", this.fogDensity);
		this.genChance = config.getFloat(id, "generation_chance", this.genChance);
		this.edgeSize = config.getInt(id, "edge_size", this.edgeSize);
		return this;
	}
	
	/**
	 * Used to load biome settings from config.
	 * @param config - {@link PathConfig}.
	 * @return this {@link BCLBiomeDef}.
	 */
	public BCLBiomeDef loadConfigValues(PathConfig config) {
		String biomePath = id.getNamespace() + "." + id.getPath();
		this.fogDensity = config.getFloat(biomePath, "fog_density", this.fogDensity);
		this.genChance = config.getFloat(biomePath, "generation_chance", this.genChance);
		this.edgeSize = config.getInt(biomePath, "edge_size", this.edgeSize);
		return this;
	}
	
	/**
	 * Set category of the biome.
	 * @param category - {@link BiomeCategory}.
	 * @return this {@link BCLBiomeDef}.
	 */
	public BCLBiomeDef setCategory(BiomeCategory category) {
		this.category = category;
		return this;
	}
	
	public BCLBiomeDef setPrecipitation(Precipitation precipitation) {
		this.precipitation = precipitation;
		return this;
	}
	
	public BCLBiomeDef setSurface(Block block) {
		setSurface(SurfaceBuilder.DEFAULT.configured(new SurfaceBuilderBaseConfiguration(
			block.defaultBlockState(),
			Blocks.END_STONE.defaultBlockState(),
			Blocks.END_STONE.defaultBlockState()
		)));
		return this;
	}
	
	public BCLBiomeDef setSurface(Block block1, Block block2) {
		setSurface(DoubleBlockSurfaceBuilder.register("bclib_" + id.getPath() + "_surface")
											.setBlock1(block1)
											.setBlock2(block2)
											.configured());
		return this;
	}
	
	public BCLBiomeDef setSurface(ConfiguredSurfaceBuilder<?> builder) {
		this.surface = builder;
		return this;
	}
	
	public BCLBiomeDef setParticles(ParticleOptions particle, float probability) {
		this.particleConfig = new AmbientParticleSettings(particle, probability);
		return this;
	}
	
	public BCLBiomeDef setGenChance(float genChance) {
		this.genChance = genChance;
		return this;
	}
	
	public BCLBiomeDef setDepth(float depth) {
		this.depth = depth;
		return this;
	}
	
	public BCLBiomeDef setTemperature(float temperature) {
		this.temperature = temperature;
		return this;
	}
	
	public BCLBiomeDef setDownfall(float downfall) {
		this.downfall = downfall;
		return this;
	}
	
	public BCLBiomeDef setEdgeSize(int edgeSize) {
		this.edgeSize = edgeSize;
		return this;
	}
	
	public BCLBiomeDef addMobSpawn(EntityType<?> type, int weight, int minGroupSize, int maxGroupSize) {
		ResourceLocation eID = Registry.ENTITY_TYPE.getKey(type);
		if (eID != Registry.ENTITY_TYPE.getDefaultKey()) {
			SpawnInfo info = new SpawnInfo();
			info.type = type;
			info.weight = weight;
			info.minGroupSize = minGroupSize;
			info.maxGroupSize = maxGroupSize;
			mobs.add(info);
		}
		return this;
	}
	
	public BCLBiomeDef addMobSpawn(SpawnerData entry) {
		spawns.add(entry);
		return this;
	}
	
	public BCLBiomeDef addStructureFeature(ConfiguredStructureFeature<?, ?> feature) {
		structures.add(feature);
		return this;
	}
	
	public BCLBiomeDef addStructureFeature(BCLStructureFeature feature) {
		structures.add(feature.getFeatureConfigured());
		return this;
	}
	
	public BCLBiomeDef addFeature(BCLFeature feature) {
		FeatureInfo info = new FeatureInfo();
		info.featureStep = feature.getFeatureStep();
		info.feature = feature.getFeatureConfigured();
		features.add(info);
		return this;
	}
	
	public BCLBiomeDef addFeature(Decoration featureStep, ConfiguredFeature<?, ?> feature) {
		FeatureInfo info = new FeatureInfo();
		info.featureStep = featureStep;
		info.feature = feature;
		features.add(info);
		return this;
	}
	
	private int getColor(int r, int g, int b) {
		r = Mth.clamp(r, 0, 255);
		g = Mth.clamp(g, 0, 255);
		b = Mth.clamp(b, 0, 255);
		return ColorUtil.color(r, g, b);
	}
	
	public BCLBiomeDef setSkyColor(int rgb) {
		this.skyColor = rgb;
		return this;
	}
	
	public BCLBiomeDef setSkyColor(int r, int g, int b) {
		return setSkyColor(getColor(r, g, b));
	}
	
	public BCLBiomeDef setFogColor(int rgb) {
		this.fogColor = rgb;
		return this;
	}
	
	public BCLBiomeDef setFogColor(int r, int g, int b) {
		return setFogColor(getColor(r, g, b));
	}
	
	public BCLBiomeDef setFogDensity(float density) {
		this.fogDensity = density;
		return this;
	}
	
	public BCLBiomeDef setWaterColor(int r, int g, int b) {
		this.waterColor = getColor(r, g, b);
		return this;
	}
	
	public BCLBiomeDef setWaterFogColor(int r, int g, int b) {
		this.waterFogColor = getColor(r, g, b);
		return this;
	}
	
	public BCLBiomeDef setWaterAndFogColor(int r, int g, int b) {
		return setWaterColor(r, g, b).setWaterFogColor(r, g, b);
	}
	
	public BCLBiomeDef setFoliageColor(int r, int g, int b) {
		this.foliageColor = getColor(r, g, b);
		return this;
	}
	
	public BCLBiomeDef setGrassColor(int r, int g, int b) {
		this.grassColor = getColor(r, g, b);
		return this;
	}
	
	public BCLBiomeDef setPlantsColor(int r, int g, int b) {
		return this.setFoliageColor(r, g, b).setGrassColor(r, g, b);
	}
	
	public BCLBiomeDef setLoop(SoundEvent loop) {
		this.loop = loop;
		return this;
	}
	
	public BCLBiomeDef setMood(SoundEvent mood) {
		this.mood = new AmbientMoodSettings(mood, 6000, 8, 2.0D);
		return this;
	}
	
	public BCLBiomeDef setAdditions(SoundEvent additions) {
		this.additions = new AmbientAdditionsSettings(additions, 0.0111);
		return this;
	}
	
	public BCLBiomeDef setMusic(SoundEvent music) {
		this.music = music;
		return this;
	}
	
	protected void addCustomToBuild(BiomeGenerationSettings.Builder generationSettings){
	
	}
	
	public Biome build() {
		MobSpawnSettings.Builder spawnSettings = new MobSpawnSettings.Builder();
		BiomeGenerationSettings.Builder generationSettings = new BiomeGenerationSettings.Builder();
		Builder effects = new Builder();
		
		mobs.forEach((spawn) -> {
			spawnSettings.addSpawn(
				spawn.type.getCategory(),
				new MobSpawnSettings.SpawnerData(spawn.type, spawn.weight, spawn.minGroupSize, spawn.maxGroupSize)
			);
		});
		
		spawns.forEach((entry) -> {
			spawnSettings.addSpawn(entry.type.getCategory(), entry);
		});
		
		generationSettings.surfaceBuilder(surface == null ? net.minecraft.data.worldgen.SurfaceBuilders.END : surface);
		structures.forEach((structure) -> generationSettings.addStructureStart(structure));
		features.forEach((info) -> generationSettings.addFeature(info.featureStep, info.feature));
		carvers.forEach((info) -> generationSettings.addCarver(info.carverStep, info.carver));
		
		addCustomToBuild(generationSettings);
		
		effects.skyColor(skyColor)
			   .waterColor(waterColor)
			   .waterFogColor(waterFogColor)
			   .fogColor(fogColor)
			   .foliageColorOverride(foliageColor)
			   .grassColorOverride(grassColor);
		if (loop != null) effects.ambientLoopSound(loop);
		if (mood != null) effects.ambientMoodSound(mood);
		if (additions != null) effects.ambientAdditionsSound(additions);
		if (particleConfig != null) effects.ambientParticle(particleConfig);
		effects.backgroundMusic(music != null ? new Music(music, 600, 2400, true) : Musics.END);
		
		return new Biome.BiomeBuilder()
			.precipitation(precipitation)
			.biomeCategory(category)
			.depth(depth)
			.scale(0.2F)
			.temperature(temperature)
			.downfall(downfall)
			.specialEffects(effects.build())
			.mobSpawnSettings(spawnSettings.build())
			.generationSettings(generationSettings.build())
			.build();
	}
	
	private static final class SpawnInfo {
		EntityType<?> type;
		int weight;
		int minGroupSize;
		int maxGroupSize;
	}
	
	private static final class FeatureInfo {
		Decoration featureStep;
		ConfiguredFeature<?, ?> feature;
	}
	
	private static final class CarverInfo <C extends CarverConfiguration> {
		Carving carverStep;
		ConfiguredWorldCarver<C> carver;
	}
	
	public ResourceLocation getID() {
		return id;
	}
	
	public float getFodDensity() {
		return fogDensity;
	}
	
	public float getGenChance() {
		return genChance;
	}
	
	public int getEdgeSize() {
		return edgeSize;
	}
	
	public <C extends CarverConfiguration> BCLBiomeDef addCarver(Carving carverStep, ConfiguredWorldCarver<C> carver) {
		CarverInfo info = new CarverInfo();
		info.carverStep = carverStep;
		info.carver = carver;
		carvers.add(info);
		return this;
	}
	
	public BCLBiomeDef addCustomData(String name, Object value) {
		customData.put(name, value);
		return this;
	}
	
	@SuppressWarnings("unchecked")
	public <T> T getCustomData(String name, Object defaultValue) {
		return (T) customData.getOrDefault(name, defaultValue);
	}
	
	protected Map<String, Object> getCustomData() {
		return customData;
	}
}