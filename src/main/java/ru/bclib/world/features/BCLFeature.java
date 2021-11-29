package ru.bclib.world.features;

import net.minecraft.core.Registry;
import net.minecraft.data.BuiltinRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.levelgen.GenerationStep;
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
import net.minecraft.world.level.levelgen.structure.templatesystem.RuleTest;
import net.minecraft.world.level.levelgen.structure.templatesystem.TagMatchTest;
import ru.bclib.api.TagAPI;

public class BCLFeature {
	private static final RuleTest ANY_TERRAIN = new TagMatchTest(TagAPI.BLOCK_GEN_TERRAIN);
	private ConfiguredFeature<?, ?> featureConfigured;
	private GenerationStep.Decoration featureStep;
	private Feature<?> feature;
	
	public BCLFeature(Feature<?> feature, ConfiguredFeature<?, ?> configuredFeature, GenerationStep.Decoration featureStep) {
		this.featureConfigured = configuredFeature;
		this.featureStep = featureStep;
		this.feature = feature;
	}
	
	public BCLFeature(ResourceLocation id, Feature<NoneFeatureConfiguration> feature, GenerationStep.Decoration featureStep, ConfiguredFeature<?, ?> configuredFeature) {
		this.featureConfigured = Registry.register(BuiltinRegistries.CONFIGURED_FEATURE, id, configuredFeature);
		this.feature = Registry.register(Registry.FEATURE, id, feature);
		this.featureStep = featureStep;
	}
	
	public static BCLFeature makeVegetationFeature(ResourceLocation id, Feature<NoneFeatureConfiguration> feature, int density) {
		ConfiguredFeature<?, ?> configured = feature
			.configured(FeatureConfiguration.NONE)
			.decorated(BCLDecorators.HEIGHTMAP_SQUARE)
			.countRandom(density);
		return new BCLFeature(id, feature, GenerationStep.Decoration.VEGETAL_DECORATION, configured);
	}
	
	public static BCLFeature makeRawGenFeature(ResourceLocation id, Feature<NoneFeatureConfiguration> feature, int chance) {
		ConfiguredFeature<?, ?> configured = feature
			.configured(FeatureConfiguration.NONE)
			.decorated(FeatureDecorator.CHANCE.configured(new ChanceDecoratorConfiguration(chance)));
		return new BCLFeature(id, feature, GenerationStep.Decoration.RAW_GENERATION, configured);
	}
	
	@Deprecated
	public static BCLFeature makeLakeFeature(ResourceLocation id, Feature<NoneFeatureConfiguration> feature, int chance) {
		ConfiguredFeature<?, ?> configured = feature
			.configured(FeatureConfiguration.NONE)
			.decorated(FeatureDecorator.LAVA_LAKE.configured(new ChanceDecoratorConfiguration(chance)));
		return new BCLFeature(id, feature, GenerationStep.Decoration.LAKES, configured);
	}
	
	public static BCLFeature makeOreFeature(ResourceLocation id, Block blockOre, Block hostBlock, int veins, int veinSize, int offset, int minY, int maxY) {
		OreConfiguration featureConfig = new OreConfiguration(
			new BlockMatchTest(hostBlock),
			blockOre.defaultBlockState(),
			veinSize
		);
		//OreConfiguration config = new OreConfiguration(ANY_TERRAIN, blockOre.defaultBlockState(), 33);
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
			GenerationStep.Decoration.UNDERGROUND_ORES
		);
	}
	
	/**
	 * Will create feature which will be generated once in each chunk.
	 * @param id {@link ResourceLocation} feature ID.
	 * @param step {@link GenerationStep.Decoration} feature step.
	 * @param feature {@link Feature} with {@link NoneFeatureConfiguration} config.
	 * @return new BCLFeature instance.
	 */
	public static BCLFeature makeChunkFeature(ResourceLocation id, GenerationStep.Decoration step, Feature<NoneFeatureConfiguration> feature) {
		ConfiguredFeature<?, ?> configured = feature
			.configured(FeatureConfiguration.NONE)
			.decorated(FeatureDecorator.COUNT.configured(new CountConfiguration(1)));
		return new BCLFeature(id, feature, step, configured);
	}
	
	@Deprecated(forRemoval = true)
	public static BCLFeature makeChunkFeature(ResourceLocation id, Feature<NoneFeatureConfiguration> feature) {
		return makeChunkFeature(id, GenerationStep.Decoration.LOCAL_MODIFICATIONS, feature);
	}
	
	public static BCLFeature makeChansedFeature(ResourceLocation id, Feature<NoneFeatureConfiguration> feature, int chance) {
		ConfiguredFeature<?, ?> configured = feature
			.configured(FeatureConfiguration.NONE)
			.decorated(FeatureDecorator.CHANCE.configured(new ChanceDecoratorConfiguration(chance)));
		return new BCLFeature(id, feature, GenerationStep.Decoration.SURFACE_STRUCTURES, configured);
	}
	
	public static BCLFeature makeCountRawFeature(ResourceLocation id, Feature<NoneFeatureConfiguration> feature, int chance) {
		ConfiguredFeature<?, ?> configured = feature.configured(FeatureConfiguration.NONE)
			.decorated(FeatureDecorator.COUNT.configured(new CountConfiguration(chance)));
		return new BCLFeature(id, feature, GenerationStep.Decoration.RAW_GENERATION, configured);
	}
	
	public static BCLFeature makeFeatureConfigured(ResourceLocation id, Feature<NoneFeatureConfiguration> feature) {
		ConfiguredFeature<?, ?> configured = feature.configured(FeatureConfiguration.NONE);
		return new BCLFeature(id, feature, GenerationStep.Decoration.RAW_GENERATION, configured);
	}
	
	public Feature<?> getFeature() {
		return feature;
	}
	
	public ConfiguredFeature<?, ?> getFeatureConfigured() {
		return featureConfigured;
	}
	
	public GenerationStep.Decoration getFeatureStep() {
		return featureStep;
	}
}
