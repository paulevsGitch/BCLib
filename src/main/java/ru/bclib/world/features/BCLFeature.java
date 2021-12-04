package ru.bclib.world.features;

import net.minecraft.core.Registry;
import net.minecraft.data.BuiltinRegistries;
import net.minecraft.data.worldgen.placement.PlacementUtils;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.valueproviders.UniformInt;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.levelgen.GenerationStep.Decoration;
import net.minecraft.world.level.levelgen.VerticalAnchor;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.configurations.FeatureConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.OreConfiguration;
import net.minecraft.world.level.levelgen.heightproviders.UniformHeight;
import net.minecraft.world.level.levelgen.placement.BiomeFilter;
import net.minecraft.world.level.levelgen.placement.CountPlacement;
import net.minecraft.world.level.levelgen.placement.HeightRangePlacement;
import net.minecraft.world.level.levelgen.placement.InSquarePlacement;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;
import net.minecraft.world.level.levelgen.placement.RarityFilter;
import net.minecraft.world.level.levelgen.structure.templatesystem.BlockMatchTest;

public class BCLFeature {
	private PlacedFeature placedFeature;
	private Decoration featureStep;
	private Feature<?> feature;
	
	public BCLFeature(Feature<?> feature, PlacedFeature placedFeature, Decoration featureStep) {
		this.placedFeature = placedFeature;
		this.featureStep = featureStep;
		this.feature = feature;
	}
	
	public BCLFeature(ResourceLocation id, Feature<NoneFeatureConfiguration> feature, Decoration featureStep, PlacedFeature placedFeature) {
		this.placedFeature = Registry.register(BuiltinRegistries.PLACED_FEATURE, id, placedFeature);
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
	 * @return {@link PlacedFeature}.
	 */
	public PlacedFeature getPlacedFeature() {
		return placedFeature;
	}
	
	/**
	 * Get feature decoration step.
	 * @return {@link Decoration}.
	 */
	public Decoration getDecoration() {
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
		PlacedFeature configured = feature
			.configured(FeatureConfiguration.NONE)
			.placed(
				CountPlacement.of(UniformInt.of(0, 4)),
				InSquarePlacement.spread(),
				PlacementUtils.HEIGHTMAP,
				BiomeFilter.biome()
			);
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
		//TODO: 1.18 See if this still works
		return makeOreFeature(id, blockOre, hostBlock, veins, veinSize, minY, maxY, false);
	}
	
	/**
	 * Will create a basic ore feature.
	 *
	 * @param id        {@link ResourceLocation} feature ID.
	 * @param blockOre  {@link Decoration} feature step.
	 * @param hostBlock {@link Block} to generate feature in.
	 * @param veins     iterations per chunk.
	 * @param veinSize  size of ore vein.
	 * @param minY      minimum height.
	 * @param maxY      maximum height.
	 * @param rare      when true, this is placed as a rare resource
	 * @return new BCLFeature instance.
	 */
	public static BCLFeature makeOreFeature(ResourceLocation id, Block blockOre, Block hostBlock, int veins, int veinSize, int minY, int maxY, boolean rare) {
		OreConfiguration featureConfig = new OreConfiguration(
			new BlockMatchTest(hostBlock),
			blockOre.defaultBlockState(),
			veinSize
		);
		
		PlacedFeature oreFeature = Feature.ORE
			.configured(featureConfig)
			.placed(
				InSquarePlacement.spread(),
				rare ? RarityFilter.onAverageOnceEvery(veins) : CountPlacement.of(veins),
				HeightRangePlacement.of(
					UniformHeight.of(
						VerticalAnchor.absolute(minY),
						VerticalAnchor.absolute(maxY)
					)
				),
				BiomeFilter.biome());
		return new BCLFeature(
			net.minecraft.world.level.levelgen.feature.Feature.ORE,
			Registry.register(BuiltinRegistries.PLACED_FEATURE, id, oreFeature),
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
		PlacedFeature configured = feature
			.configured(FeatureConfiguration.NONE)
			.placed(CountPlacement.of(1));
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
		PlacedFeature configured = feature
			.configured(FeatureConfiguration.NONE)
			.placed(RarityFilter.onAverageOnceEvery(chance));
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
		PlacedFeature configured = feature
			.configured(FeatureConfiguration.NONE)
			.placed(CountPlacement.of(count));
			//.decorated(FeatureDecorator.COUNT.configured(new CountConfiguration(count)));
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
		PlacedFeature configured = feature.configured(FeatureConfiguration.NONE).placed();
		return new BCLFeature(id, feature, step, configured);
	}
}
