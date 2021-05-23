package ru.bclib.world.features;

import net.minecraft.core.Registry;
import net.minecraft.data.BuiltinRegistries;
import net.minecraft.data.worldgen.Features;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.configurations.CountConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.FeatureConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.OreConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.RangeDecoratorConfiguration;
import net.minecraft.world.level.levelgen.placement.ChanceDecoratorConfiguration;
import net.minecraft.world.level.levelgen.placement.FeatureDecorator;
import net.minecraft.world.level.levelgen.structure.templatesystem.BlockMatchTest;

public class BCLFeature {
	private Feature<?> feature;
	private ConfiguredFeature<?, ?> featureConfigured;
	private GenerationStep.Decoration featureStep;
	
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
		ConfiguredFeature<?, ?> configured = feature.configured(FeatureConfiguration.NONE).decorated(Features.Decorators.HEIGHTMAP_SQUARE).countRandom(density);
		return new BCLFeature(id, feature, GenerationStep.Decoration.VEGETAL_DECORATION, configured);
	}
	
	public static BCLFeature makeRawGenFeature(ResourceLocation id, Feature<NoneFeatureConfiguration> feature, int chance) {
		ConfiguredFeature<?, ?> configured = feature.configured(FeatureConfiguration.NONE).decorated(FeatureDecorator.CHANCE.configured(new ChanceDecoratorConfiguration(chance)));
		return new BCLFeature(id, feature, GenerationStep.Decoration.RAW_GENERATION, configured);
	}
	
	public static BCLFeature makeLakeFeature(ResourceLocation id, Feature<NoneFeatureConfiguration> feature, int chance) {
		ConfiguredFeature<?, ?> configured = feature.configured(FeatureConfiguration.NONE).decorated(FeatureDecorator.WATER_LAKE.configured(new ChanceDecoratorConfiguration(chance)));
		return new BCLFeature(id, feature, GenerationStep.Decoration.LAKES, configured);
	}
	
	public static BCLFeature makeOreFeature(ResourceLocation id, Block blockOre, int veins, int veinSize, int offset, int minY, int maxY) {
		OreConfiguration featureConfig = new OreConfiguration(new BlockMatchTest(Blocks.END_STONE), blockOre.defaultBlockState(), veinSize);
		RangeDecoratorConfiguration rangeDecorator = new RangeDecoratorConfiguration(offset, minY, maxY);
		ConfiguredFeature<?, ?> oreFeature = Feature.ORE.configured(featureConfig)
			  .decorated(FeatureDecorator.RANGE.configured(rangeDecorator))
			  .squared()
			  .count(veins);
		return new BCLFeature(Feature.ORE, Registry.register(BuiltinRegistries.CONFIGURED_FEATURE, id, oreFeature), GenerationStep.Decoration.UNDERGROUND_ORES);
	}
	
	public static BCLFeature makeChunkFeature(ResourceLocation id, Feature<NoneFeatureConfiguration> feature) {
		ConfiguredFeature<?, ?> configured = feature.configured(FeatureConfiguration.NONE).decorated(FeatureDecorator.COUNT.configured(new CountConfiguration(1)));
		return new BCLFeature(id, feature, GenerationStep.Decoration.LOCAL_MODIFICATIONS, configured);
	}
	
	public static BCLFeature makeChansedFeature(ResourceLocation id, Feature<NoneFeatureConfiguration> feature, int chance) {
		ConfiguredFeature<?, ?> configured = feature.configured(FeatureConfiguration.NONE).decorated(FeatureDecorator.CHANCE.configured(new ChanceDecoratorConfiguration(chance)));
		return new BCLFeature(id, feature, GenerationStep.Decoration.SURFACE_STRUCTURES, configured);
	}
	
	public static BCLFeature makeCountRawFeature(ResourceLocation id, Feature<NoneFeatureConfiguration> feature, int chance) {
		ConfiguredFeature<?, ?> configured = feature.configured(FeatureConfiguration.NONE).decorated(FeatureDecorator.COUNT.configured(new CountConfiguration(chance)));
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
