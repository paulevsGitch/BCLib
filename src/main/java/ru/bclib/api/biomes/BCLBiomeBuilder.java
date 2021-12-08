package ru.bclib.api.biomes;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Supplier;

import net.fabricmc.fabric.api.biome.v1.BiomeModifications;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.data.BuiltinRegistries;
import net.minecraft.data.worldgen.BiomeDefaultFeatures;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.Music;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.biome.AmbientAdditionsSettings;
import net.minecraft.world.level.biome.AmbientMoodSettings;
import net.minecraft.world.level.biome.AmbientParticleSettings;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Biome.BiomeBuilder;
import net.minecraft.world.level.biome.Biome.BiomeCategory;
import net.minecraft.world.level.biome.Biome.Precipitation;
import net.minecraft.world.level.biome.BiomeGenerationSettings;
import net.minecraft.world.level.biome.BiomeSpecialEffects;
import net.minecraft.world.level.biome.MobSpawnSettings;
import net.minecraft.world.level.biome.MobSpawnSettings.SpawnerData;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.GenerationStep.Decoration;
import net.minecraft.world.level.levelgen.Noises;
import net.minecraft.world.level.levelgen.SurfaceRules;
import net.minecraft.world.level.levelgen.carver.ConfiguredWorldCarver;
import net.minecraft.world.level.levelgen.feature.ConfiguredStructureFeature;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;
import ru.bclib.mixin.common.BiomeGenerationSettingsAccessor;
import ru.bclib.util.CollectionsUtil;
import ru.bclib.util.ColorUtil;
import ru.bclib.world.biomes.BCLBiome;
import ru.bclib.world.features.BCLFeature;
import ru.bclib.world.structures.BCLStructureFeature;

public class BCLBiomeBuilder {
	private static final BCLBiomeBuilder INSTANCE = new BCLBiomeBuilder();
	private static final SurfaceRules.ConditionSource SURFACE_NOISE = SurfaceRules.noiseCondition(Noises.SOUL_SAND_LAYER, -0.012);
	
	private List<ConfiguredStructureFeature> structures = new ArrayList<>(16);
	private BiomeGenerationSettings.Builder generationSettings;
	private BiomeSpecialEffects.Builder effectsBuilder;
	private MobSpawnSettings.Builder spawnSettings;
	private SurfaceRules.RuleSource surfaceRule;
	private Precipitation precipitation;
	private ResourceLocation biomeID;
	private BiomeCategory category;
	private float temperature;
	private float fogDensity;
	private float genChance;
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
		INSTANCE.generationSettings = null;
		INSTANCE.effectsBuilder = null;
		INSTANCE.spawnSettings = null;
		INSTANCE.structures.clear();
		INSTANCE.temperature = 1.0F;
		INSTANCE.fogDensity = 1.0F;
		INSTANCE.downfall = 1.0F;
		INSTANCE.genChance = 1.0F;
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
	 * @return same {@link BCLBiomeBuilder} instance.
	 */
	public <M extends Mob> BCLBiomeBuilder spawn(EntityType<M> entityType, int weight, int minGroupCount, int maxGroupCount) {
		getSpawns().addSpawn(entityType.getCategory(), new SpawnerData(entityType, weight, minGroupCount, maxGroupCount));
		return this;
	}
	
	/**
	 * Adds ambient particles to thr biome.
	 * @param particle {@link ParticleOptions} particles (or {@link net.minecraft.core.particles.ParticleType}).
	 * @param probability particle spawn probability, should have low value (example: 0.01F).
	 * @return same {@link BCLBiomeBuilder} instance.
	 */
	public BCLBiomeBuilder particles(ParticleOptions particle, float probability) {
		getEffects().ambientParticle(new AmbientParticleSettings(particle, probability));
		return this;
	}
	
	/**
	 * Sets sky color for the biome. Color is in ARGB int format.
	 * @param color ARGB color as integer.
	 * @return same {@link BCLBiomeBuilder} instance.
	 */
	public BCLBiomeBuilder skyColor(int color) {
		getEffects().skyColor(color);
		return this;
	}
	
	/**
	 * Sets sky color for the biome. Color represented as red, green and blue channel values.
	 * @param red red color component [0-255]
	 * @param green green color component [0-255]
	 * @param blue blue color component [0-255]
	 * @return same {@link BCLBiomeBuilder} instance.
	 */
	public BCLBiomeBuilder skyColor(int red, int green, int blue) {
		red = Mth.clamp(red, 0, 255);
		green = Mth.clamp(green, 0, 255);
		blue = Mth.clamp(blue, 0, 255);
		return skyColor(ColorUtil.color(red, green, blue));
	}
	
	/**
	 * Sets fog color for the biome. Color is in ARGB int format.
	 * @param color ARGB color as integer.
	 * @return same {@link BCLBiomeBuilder} instance.
	 */
	public BCLBiomeBuilder fogColor(int color) {
		getEffects().fogColor(color);
		return this;
	}
	
	/**
	 * Sets fog color for the biome. Color represented as red, green and blue channel values.
	 * @param red red color component [0-255]
	 * @param green green color component [0-255]
	 * @param blue blue color component [0-255]
	 * @return same {@link BCLBiomeBuilder} instance.
	 */
	public BCLBiomeBuilder fogColor(int red, int green, int blue) {
		red = Mth.clamp(red, 0, 255);
		green = Mth.clamp(green, 0, 255);
		blue = Mth.clamp(blue, 0, 255);
		return fogColor(ColorUtil.color(red, green, blue));
	}
	
	/**
	 * Sets fog density for the biome.
	 * @param density fog density as a float, default value is 1.0F.
	 * @return same {@link BCLBiomeBuilder} instance.
	 */
	public BCLBiomeBuilder fogDensity(float density) {
		this.fogDensity = density;
		return this;
	}

	/**
	 * Sets generation chance for this biome.
	 * @param genChance
	 * @return same {@link BCLBiomeBuilder}.
	 */
	public BCLBiomeBuilder genChance(float genChance) {
		this.genChance = genChance;
		return this;
	}
	
	/**
	 * Sets water color for the biome. Color is in ARGB int format.
	 * @param color ARGB color as integer.
	 * @return same {@link BCLBiomeBuilder} instance.
	 */
	public BCLBiomeBuilder waterColor(int color) {
		getEffects().waterColor(color);
		return this;
	}
	
	/**
	 * Sets water color for the biome. Color represented as red, green and blue channel values.
	 * @param red red color component [0-255]
	 * @param green green color component [0-255]
	 * @param blue blue color component [0-255]
	 * @return same {@link BCLBiomeBuilder} instance.
	 */
	public BCLBiomeBuilder waterColor(int red, int green, int blue) {
		red = Mth.clamp(red, 0, 255);
		green = Mth.clamp(green, 0, 255);
		blue = Mth.clamp(blue, 0, 255);
		return waterColor(ColorUtil.color(red, green, blue));
	}
	
	/**
	 * Sets underwater fog color for the biome. Color is in ARGB int format.
	 * @param color ARGB color as integer.
	 * @return same {@link BCLBiomeBuilder} instance.
	 */
	public BCLBiomeBuilder waterFogColor(int color) {
		getEffects().waterFogColor(color);
		return this;
	}
	
	/**
	 * Sets underwater fog color for the biome. Color represented as red, green and blue channel values.
	 * @param red red color component [0-255]
	 * @param green green color component [0-255]
	 * @param blue blue color component [0-255]
	 * @return same {@link BCLBiomeBuilder} instance.
	 */
	public BCLBiomeBuilder waterFogColor(int red, int green, int blue) {
		red = Mth.clamp(red, 0, 255);
		green = Mth.clamp(green, 0, 255);
		blue = Mth.clamp(blue, 0, 255);
		return waterFogColor(ColorUtil.color(red, green, blue));
	}
	
	/**
	 * Sets water and underwater fig color for the biome. Color is in ARGB int format.
	 * @param color ARGB color as integer.
	 * @return same {@link BCLBiomeBuilder} instance.
	 */
	public BCLBiomeBuilder waterAndFogColor(int color) {
		return waterColor(color).waterFogColor(color);
	}
	
	/**
	 * Sets water and underwater fig color for the biome. Color is in ARGB int format.
	 * @param red red color component [0-255]
	 * @param green green color component [0-255]
	 * @param blue blue color component [0-255]
	 * @return same {@link BCLBiomeBuilder} instance.
	 */
	public BCLBiomeBuilder waterAndFogColor(int red, int green, int blue) {
		red = Mth.clamp(red, 0, 255);
		green = Mth.clamp(green, 0, 255);
		blue = Mth.clamp(blue, 0, 255);
		return waterAndFogColor(ColorUtil.color(red, green, blue));
	}
	
	/**
	 * Sets grass color for the biome. Color is in ARGB int format.
	 * @param color ARGB color as integer.
	 * @return same {@link BCLBiomeBuilder} instance.
	 */
	public BCLBiomeBuilder grassColor(int color) {
		getEffects().grassColorOverride(color);
		return this;
	}
	
	/**
	 * Sets grass color for the biome. Color represented as red, green and blue channel values.
	 * @param red red color component [0-255]
	 * @param green green color component [0-255]
	 * @param blue blue color component [0-255]
	 * @return same {@link BCLBiomeBuilder} instance.
	 */
	public BCLBiomeBuilder grassColor(int red, int green, int blue) {
		red = Mth.clamp(red, 0, 255);
		green = Mth.clamp(green, 0, 255);
		blue = Mth.clamp(blue, 0, 255);
		return grassColor(ColorUtil.color(red, green, blue));
	}
	
	/**
	 * Sets leaves and plants color for the biome. Color is in ARGB int format.
	 * @param color ARGB color as integer.
	 * @return same {@link BCLBiomeBuilder} instance.
	 */
	public BCLBiomeBuilder foliageColor(int color) {
		getEffects().foliageColorOverride(color);
		return this;
	}
	
	/**
	 * Sets leaves and plants color for the biome. Color represented as red, green and blue channel values.
	 * @param red red color component [0-255]
	 * @param green green color component [0-255]
	 * @param blue blue color component [0-255]
	 * @return same {@link BCLBiomeBuilder} instance.
	 */
	public BCLBiomeBuilder foliageColor(int red, int green, int blue) {
		red = Mth.clamp(red, 0, 255);
		green = Mth.clamp(green, 0, 255);
		blue = Mth.clamp(blue, 0, 255);
		return foliageColor(ColorUtil.color(red, green, blue));
	}
	
	/**
	 * Sets grass, leaves and all plants color for the biome. Color is in ARGB int format.
	 * @param color ARGB color as integer.
	 * @return same {@link BCLBiomeBuilder} instance.
	 */
	public BCLBiomeBuilder plantsColor(int color) {
		return grassColor(color).foliageColor(color);
	}
	
	/**
	 * Sets grass, leaves and all plants color for the biome. Color represented as red, green and blue channel values.
	 * @param red red color component [0-255]
	 * @param green green color component [0-255]
	 * @param blue blue color component [0-255]
	 * @return same {@link BCLBiomeBuilder} instance.
	 */
	public BCLBiomeBuilder plantsColor(int red, int green, int blue) {
		red = Mth.clamp(red, 0, 255);
		green = Mth.clamp(green, 0, 255);
		blue = Mth.clamp(blue, 0, 255);
		return plantsColor(ColorUtil.color(red, green, blue));
	}
	
	/**
	 * Sets biome music, used for biomes in the Nether and End.
	 * @param music {@link Music} to use.
	 * @return same {@link BCLBiomeBuilder} instance.
	 */
	public BCLBiomeBuilder music(Music music) {
		getEffects().backgroundMusic(music);
		return this;
	}
	
	/**
	 * Sets biome music, used for biomes in the Nether and End.
	 * @param music {@link SoundEvent} to use.
	 * @return same {@link BCLBiomeBuilder} instance.
	 */
	public BCLBiomeBuilder music(SoundEvent music) {
		return music(new Music(music, 600, 2400, true));
	}
	
	/**
	 * Sets biome ambient loop sound. Can be used for biome environment.
	 * @param loopSound {@link SoundEvent} to use as a loop.
	 * @return same {@link BCLBiomeBuilder} instance.
	 */
	public BCLBiomeBuilder loop(SoundEvent loopSound) {
		getEffects().ambientLoopSound(loopSound);
		return this;
	}
	
	/**
	 * Sets biome mood sound. Can be used for biome environment.
	 * @param mood {@link SoundEvent} to use as a mood.
	 * @param tickDelay delay between sound events in ticks.
	 * @param blockSearchExtent block search radius (for area available for sound).
	 * @param soundPositionOffset offset in sound.
	 * @return same {@link BCLBiomeBuilder} instance.
	 */
	public BCLBiomeBuilder mood(SoundEvent mood, int tickDelay, int blockSearchExtent, float soundPositionOffset) {
		getEffects().ambientMoodSound(new AmbientMoodSettings(mood, tickDelay, blockSearchExtent, soundPositionOffset));
		return this;
	}
	
	/**
	 * Sets biome mood sound. Can be used for biome environment.
	 * @param mood {@link SoundEvent} to use as a mood.
	 * @return same {@link BCLBiomeBuilder} instance.
	 */
	public BCLBiomeBuilder mood(SoundEvent mood) {
		return mood(mood, 6000, 8, 2.0F);
	}
	
	/**
	 * Sets biome additionsl ambient sounds.
	 * @param additions {@link SoundEvent} to use.
	 * @param intensity sound intensity. Default is 0.0111F.
	 * @return same {@link BCLBiomeBuilder} instance.
	 */
	public BCLBiomeBuilder additions(SoundEvent additions, float intensity) {
		getEffects().ambientAdditionsSound(new AmbientAdditionsSettings(additions, intensity));
		return this;
	}
	
	/**
	 * Sets biome additionsl ambient sounds.
	 * @param additions {@link SoundEvent} to use.
	 * @return same {@link BCLBiomeBuilder} instance.
	 */
	public BCLBiomeBuilder additions(SoundEvent additions) {
		return additions(additions, 0.0111F);
	}
	
	/**
	 * Adds new feature to the biome.
	 * @param decoration {@link Decoration} feature step.
	 * @param feature {@link PlacedFeature}.
	 * @return same {@link BCLBiomeBuilder} instance.
	 */
	public BCLBiomeBuilder feature(Decoration decoration, PlacedFeature feature) {
		getGeneration().addFeature(decoration, feature);
		return this;
	}
	
	/**
	 * Adds vanilla Mushrooms.
	 * @return same {@link BCLBiomeBuilder} instance.
	 */
	public BCLBiomeBuilder defaultMushrooms() {
		return feature(BiomeDefaultFeatures::addDefaultMushrooms);
	}
	
	/**
	 * Adds vanilla Nether Ores.
	 * @return same {@link BCLBiomeBuilder} instance.
	 */
	public BCLBiomeBuilder netherDefaultOres() {
		return feature(BiomeDefaultFeatures::addNetherDefaultOres);
	}
	
	/**
	 * Will add features into biome, used for vanilla feature adding functions.
	 * @param featureAdd {@link Consumer} with {@link BiomeGenerationSettings.Builder}.
	 * @return same {@link BCLBiomeBuilder} instance.
	 */
	public BCLBiomeBuilder feature(Consumer<BiomeGenerationSettings.Builder> featureAdd) {
		featureAdd.accept(getGeneration());
		return this;
	}
	
	/**
	 * Adds new feature to the biome.
	 * @param feature {@link BCLFeature}.
	 * @return same {@link BCLBiomeBuilder} instance.
	 */
	public BCLBiomeBuilder feature(BCLFeature feature) {
		return feature(feature.getDecoration(), feature.getPlacedFeature());
	}
	
	/**
	 * Adds new structure feature into the biome.
	 * @param structure {@link ConfiguredStructureFeature} to add.
	 * @return same {@link BCLBiomeBuilder} instance.
	 */
	public BCLBiomeBuilder structure(ConfiguredStructureFeature<?, ?> structure) {
		structures.add(structure);
		return this;
	}
	
	/**
	 * Adds new structure feature into thr biome. Will add building biome into the structure list.
	 * @param structure {@link BCLStructureFeature} to add.
	 * @return same {@link BCLBiomeBuilder} instance.
	 */
	public BCLBiomeBuilder structure(BCLStructureFeature structure) {
		structure.addInternalBiome(biomeID);
		return structure(structure.getFeatureConfigured());
	}
	
	/**
	 * Adds new world carver into the biome.
	 * @param carver {@link ConfiguredWorldCarver} to add.
	 * @return same {@link BCLBiomeBuilder} instance.
	 */
	public BCLBiomeBuilder carver(GenerationStep.Carving step, ConfiguredWorldCarver<?> carver) {
		BuiltinRegistries.CONFIGURED_CARVER
			.getResourceKey(carver)
			.ifPresent(key -> BiomeModifications.addCarver(ctx -> ctx.getBiomeKey().location().equals(biomeID), step, key));
		return this;
	}
	
	/**
	 * Adds new world surface rule for the given block
	 * @param surfaceBlock {@link Block} to use.
	 * @return same {@link BCLBiomeBuilder} instance.
	 */
	public BCLBiomeBuilder surface(Block surfaceBlock) {
		return surface(SurfaceRuleBuilder.start().surface(surfaceBlock.defaultBlockState()).build());
	}
	
	/**
	 * Adds blocks to the biome surface and below it (with specified depth).
	 * @param surfaceBlock {@link Block} that will cover biome.
	 * @param subterrainBlock {@link Block} below it with specified depth.
	 * @param depth thickness of bottom block layer.
	 * @return same {@link BCLBiomeBuilder} instance.
	 */
	public BCLBiomeBuilder surface(Block surfaceBlock, Block subterrainBlock, int depth) {
		return surface(SurfaceRuleBuilder
			.start()
			.surface(surfaceBlock.defaultBlockState())
			.subsurface(subterrainBlock.defaultBlockState(), depth)
			.build());
	}

	public BCLBiomeBuilder chancedSurface(SurfaceRules.RuleSource surfaceBlockA, SurfaceRules.RuleSource surfaceBlockB, SurfaceRules.RuleSource underBlock){
		return surface(
			SurfaceRules.sequence(
				SurfaceRules.ifTrue(SurfaceRules.ON_FLOOR,
					SurfaceRules.sequence(
						SurfaceRules.ifTrue(SurfaceRules.noiseCondition(Noises.SURFACE, -0.1818, 0.1818), surfaceBlockA),
						surfaceBlockB
					)
				),
				underBlock
			)
		);
	}
	
	/**
	 * Adds surface rule to this biome.
	 * @param surfaceRule {link SurfaceRules.RuleSource} surface rule.
	 * @return same {@link BCLBiomeBuilder} instance.
	 */
	public BCLBiomeBuilder surface(SurfaceRules.RuleSource surfaceRule) {
		this.surfaceRule = surfaceRule;
		return this;
	}
	
	/**
	 * Finalize biome creation.
	 * @return created {@link BCLBiome} instance.
	 */
	public BCLBiome build() {
		return build(BCLBiome::new);
	}
	
	/**
	 * Finalize biome creation.
	 * @param biomeConstructor {@link BiFunction} biome constructor.
	 * @return created {@link BCLBiome} instance.
	 */
	public <T extends BCLBiome> T build(BiFunction<ResourceLocation, Biome, T> biomeConstructor) {
		BiomeBuilder builder = new BiomeBuilder()
			.precipitation(precipitation)
			.biomeCategory(category)
			.temperature(temperature)
			.downfall(downfall);
		
		//Some biomes may not spawn entities, without this creation will due to a null spawn object
		builder.mobSpawnSettings(getSpawns().build());

		
		if (effectsBuilder != null) {
			builder.specialEffects(effectsBuilder.build());
		}
		
		if (generationSettings != null) {
			BiomeGenerationSettings settings = generationSettings.build();
			BiomeGenerationSettingsAccessor accessor = BiomeGenerationSettingsAccessor.class.cast(settings);
			List<List<Supplier<PlacedFeature>>> featureLists = CollectionsUtil.getMutable(accessor.bclib_getFeatures());
			final int size = featureLists.size();
			for (int i = 0; i < size; i++) {
				List<Supplier<PlacedFeature>> list = CollectionsUtil.getMutable(featureLists.get(i));
				BiomeAPI.sortFeatures(list);
				featureLists.add(i, list);
			}
			accessor.bclib_setFeatures(featureLists);
			builder.generationSettings(settings);
		}
		
		final T res = biomeConstructor.apply(biomeID, builder.build());
		res.attachStructures(structures);
		res.setSurface(surfaceRule);
		res.setFogDensity(fogDensity);
		res.setGenChance(genChance);
		return res;
	}
	
	/**
	 * Get or create {@link BiomeSpecialEffects.Builder} for biome visual effects.
	 * For internal usage only.
	 * For internal usage only.
	 * @return new or same {@link BiomeSpecialEffects.Builder} instance.
	 */
	private BiomeSpecialEffects.Builder getEffects() {
		if (effectsBuilder == null) {
			effectsBuilder = new BiomeSpecialEffects.Builder();
		}
		return effectsBuilder;
	}
	
	/**
	 * Get or create {@link MobSpawnSettings.Builder} for biome mob spawning.
	 * For internal usage only.
	 * @return new or same {@link MobSpawnSettings.Builder} instance.
	 */
	private MobSpawnSettings.Builder getSpawns() {
		if (spawnSettings == null) {
			spawnSettings = new MobSpawnSettings.Builder();
		}
		return spawnSettings;
	}
	
	/**
	 * Get or create {@link BiomeGenerationSettings.Builder} for biome features and generation.
	 * For internal usage only.
	 * @return new or same {@link BiomeGenerationSettings.Builder} instance.
	 */
	private BiomeGenerationSettings.Builder getGeneration() {
		if (generationSettings == null) {
			generationSettings = new BiomeGenerationSettings.Builder();
		}
		return generationSettings;
	}
}
