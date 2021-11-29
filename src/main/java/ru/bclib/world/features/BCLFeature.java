package ru.bclib.world.features;

import net.minecraft.core.Registry;
import net.minecraft.data.BuiltinRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.levelgen.GenerationStep.Decoration;
import net.minecraft.world.level.levelgen.VerticalAnchor;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.configurations.CountConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.FeatureConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.OreConfiguration;
import net.minecraft.world.level.levelgen.placement.ChanceDecoratorConfiguration;
import net.minecraft.world.level.levelgen.placement.FeatureDecorator;
import net.minecraft.world.level.levelgen.structure.templatesystem.BlockMatchTest;

public class BCLFeature {
	private ConfiguredFeature<?, ?> featureConfigured;
	private Decoration featureStep;
	private Feature<?> feature;
	
	public BCLFeature(Feature<?> feature, ConfiguredFeature<?, ?> configuredFeature, Decoration featureStep) {
		this.featureConfigured = configuredFeature;
		this.featureStep = featureStep;
		this.feature = feature;
	}
	
	public BCLFeature(ResourceLocation id, Feature<NoneFeatureConfiguration> feature, Decoration featureStep, ConfiguredFeature<?, ?> configuredFeature) {
		this.featureConfigured = Registry.register(BuiltinRegistries.CONFIGURED_FEATURE, id, configuredFeature);
		this.feature = Registry.register(Registry.FEATURE, id, feature);
		this.featureStep = featureStep;
	}
	
	/**
	 * Get raw feature.
	 * @return {@link Feature}.
	 */
	public Feature<?> getFeature() {
		return feature;
	}
	
	/**
	 * Get configured feature.
	 * @return {@link ConfiguredFeature}.
	 */
	public ConfiguredFeature<?, ?> getFeatureConfigured() {
		return featureConfigured;
	}
	
	/**
	 * Get feature decoration step.
	 * @return {@link Decoration}.
	 */
	public Decoration getFeatureStep() {
		return featureStep;
	}
	
	/**
	 * Will create a basic plant feature.
	 * @param id {@link ResourceLocation} feature ID.
	 * @param feature {@link Feature} with {@link NoneFeatureConfiguration} config.
	 * @param density iterations per chunk.
	 * @return new BCLFeature instance.
	 */
	public static BCLFeature makeVegetationFeature(ResourceLocation id, Feature<NoneFeatureConfiguration> feature, int density) {
		ConfiguredFeature<?, ?> configured = feature
			.configured(FeatureConfiguration.NONE)
			.decorated(BCLDecorators.HEIGHTMAP_SQUARE)
			.countRandom(density);
		return new BCLFeature(id, feature, Decoration.VEGETAL_DECORATION, configured);
	}
	
	/**
	 * Will create a basic ore feature.
	 * @param id {@link ResourceLocation} feature ID.
	 * @param blockOre {@link Decoration} feature step.
	 * @param hostBlock {@link Block} to generate feature in.
	 * @param veins iterations per chunk.
	 * @param veinSize size of ore vein.
	 * @param minY minimum height.
	 * @param maxY maximum height.
	 * @return new BCLFeature instance.
	 */
	public static BCLFeature makeOreFeature(ResourceLocation id, Block blockOre, Block hostBlock, int veins, int veinSize, int minY, int maxY) {
		OreConfiguration featureConfig = new OreConfiguration(
			new BlockMatchTest(hostBlock),
			blockOre.defaultBlockState(),
			veinSize
		);
		ConfiguredFeature<?, ?> oreFeature = Feature.ORE
			.configured(featureConfig)
			.rangeUniform(
				VerticalAnchor.absolute(minY),
				VerticalAnchor.absolute(maxY)
			)
			.squared()
			.count(veins);
		return new BCLFeature(
			Feature.ORE,
			Registry.register(BuiltinRegistries.CONFIGURED_FEATURE, id, oreFeature),
			Decoration.UNDERGROUND_ORES
		);
	}
	
	/**
	 * Will create feature which will be generated once in each chunk.
	 * @param id {@link ResourceLocation} feature ID.
	 * @param step {@link Decoration} feature step.
	 * @param feature {@link Feature} with {@link NoneFeatureConfiguration} config.
	 * @return new BCLFeature instance.
	 */
	public static BCLFeature makeChunkFeature(ResourceLocation id, Decoration step, Feature<NoneFeatureConfiguration> feature) {
		ConfiguredFeature<?, ?> configured = feature
			.configured(FeatureConfiguration.NONE)
			.decorated(FeatureDecorator.COUNT.configured(new CountConfiguration(1)));
		return new BCLFeature(id, feature, step, configured);
	}
	
	/**
	 * Will create feature with chanced decoration, chance for feature to generate per chunk is 1 / chance.
	 * @param id {@link ResourceLocation} feature ID.
	 * @param step {@link Decoration} feature step.
	 * @param feature {@link Feature} with {@link NoneFeatureConfiguration} config.
	 * @param chance chance for feature to be generated in.
	 * @return new BCLFeature instance.
	 */
	public static BCLFeature makeChancedFeature(ResourceLocation id, Decoration step, Feature<NoneFeatureConfiguration> feature, int chance) {
		ConfiguredFeature<?, ?> configured = feature
			.configured(FeatureConfiguration.NONE)
			.decorated(FeatureDecorator.CHANCE.configured(new ChanceDecoratorConfiguration(chance)));
		return new BCLFeature(id, feature, step, configured);
	}
	
	/**
	 * Will create feature with specified generation iterations per chunk.
	 * @param id {@link ResourceLocation} feature ID.
	 * @param step {@link Decoration} feature step.
	 * @param feature {@link Feature} with {@link NoneFeatureConfiguration} config.
	 * @param count iterations steps.
	 * @return new BCLFeature instance.
	 */
	public static BCLFeature makeCountFeature(ResourceLocation id, Decoration step, Feature<NoneFeatureConfiguration> feature, int count) {
		ConfiguredFeature<?, ?> configured = feature
			.configured(FeatureConfiguration.NONE)
			.decorated(FeatureDecorator.COUNT.configured(new CountConfiguration(count)));
		return new BCLFeature(id, feature, step, configured);
	}
	
	/**
	 * Makes simple configured feature with {@link NoneFeatureConfiguration} set to NONE.
	 * @param id {@link ResourceLocation} feature ID.
	 * @param step {@link Decoration} feature step.
	 * @param feature {@link Feature} with {@link NoneFeatureConfiguration} config.
	 * @return new BCLFeature instance.
	 */
	public static BCLFeature makeFeatureConfigured(ResourceLocation id, Decoration step, Feature<NoneFeatureConfiguration> feature) {
		ConfiguredFeature<?, ?> configured = feature.configured(FeatureConfiguration.NONE);
		return new BCLFeature(id, feature, step, configured);
	}
	
	@Deprecated(forRemoval = true)
	public static BCLFeature makeOreFeature(ResourceLocation id, Block blockOre, Block hostBlock, int veins, int veinSize, int offset, int minY, int maxY) {
		return makeOreFeature(id, blockOre, hostBlock, veins, veinSize, minY, maxY);
	}
	
	@Deprecated(forRemoval = true)
	public static BCLFeature makeRawGenFeature(ResourceLocation id, Feature<NoneFeatureConfiguration> feature, int chance) {
		return makeChancedFeature(id, Decoration.RAW_GENERATION, feature, chance);
	}
	
	@Deprecated(forRemoval = true)
	public static BCLFeature makeChunkFeature(ResourceLocation id, Feature<NoneFeatureConfiguration> feature) {
		return makeChunkFeature(id, Decoration.LOCAL_MODIFICATIONS, feature);
	}
	
	@Deprecated(forRemoval = true)
	public static BCLFeature makeChansedFeature(ResourceLocation id, Feature<NoneFeatureConfiguration> feature, int chance) {
		return makeChancedFeature(id, Decoration.SURFACE_STRUCTURES, feature, chance);
	}
	
	@Deprecated(forRemoval = true)
	public static BCLFeature makeCountRawFeature(ResourceLocation id, Feature<NoneFeatureConfiguration> feature, int chance) {
		return makeCountFeature(id, Decoration.RAW_GENERATION, feature, chance);
	}
	
	@Deprecated(forRemoval = true)
	public static BCLFeature makeFeatureConfigured(ResourceLocation id, Feature<NoneFeatureConfiguration> feature) {
		return makeFeatureConfigured(id, Decoration.RAW_GENERATION, feature);
	}
}
