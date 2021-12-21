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
import net.minecraft.world.level.levelgen.placement.BiomeFilter;
import net.minecraft.world.level.levelgen.placement.CountOnEveryLayerPlacement;
import net.minecraft.world.level.levelgen.placement.CountPlacement;
import net.minecraft.world.level.levelgen.placement.HeightRangePlacement;
import net.minecraft.world.level.levelgen.placement.InSquarePlacement;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;
import net.minecraft.world.level.levelgen.placement.PlacementModifier;
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
	
	public BCLFeature(ResourceLocation id, Feature<?> feature, Decoration featureStep, PlacedFeature placedFeature) {
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
		return makeVegetationFeature(id, feature, density, false);
	}
	
	/**
	 * Will create a basic plant feature.
	 * @param id {@link ResourceLocation} feature ID.
	 * @param feature {@link Feature} with {@link NoneFeatureConfiguration} config.
	 * @param density iterations per chunk.
	 * @param allHeight if {@code true} will generate plant on all layers, if {@code false} - only on surface.
	 * @return new BCLFeature instance.
	 */
	public static BCLFeature makeVegetationFeature(ResourceLocation id, Feature<NoneFeatureConfiguration> feature, int density, boolean allHeight) {
		if (allHeight) {
			@SuppressWarnings("deprecation")
			PlacementModifier count =CountOnEveryLayerPlacement.of(density);
			return makeFeature(id, Decoration.VEGETAL_DECORATION, feature, count, BiomeFilter.biome());
		}
		else {
			return makeFeature(id, Decoration.VEGETAL_DECORATION, feature,
				CountPlacement.of(UniformInt.of(0, density)),
				InSquarePlacement.spread(),
				PlacementUtils.HEIGHTMAP,
				BiomeFilter.biome()
			);
		}
	}
	
	/**
	 * Will create a basic ore feature.
	 * @param id {@link ResourceLocation} feature ID.
	 * @param blockOre {@link Decoration} feature step.
	 * @param hostBlock {@link Block} to generate feature in.
	 * @param veins iterations per chunk.
	 * @param veinSize size of ore vein.
	 * @param minY minimum height
	 * @param maxY maximum height.
	 * @return new BCLFeature instance.
	 */
	public static BCLFeature makeOreFeature(ResourceLocation id, Block blockOre, Block hostBlock, int veins, int veinSize, int minY, int maxY) {
		return makeOreFeature(id, blockOre, hostBlock, veins, veinSize, VerticalAnchor.absolute(minY), VerticalAnchor.absolute(maxY), false);
	}
	
	/**
	 * Will create a basic ore feature.
	 * @param id {@link ResourceLocation} feature ID.
	 * @param blockOre {@link Decoration} feature step.
	 * @param hostBlock {@link Block} to generate feature in.
	 * @param veins iterations per chunk.
	 * @param veinSize size of ore vein.
	 * @param airDiscardChance chance that this orge gets discarded when it is exposed to air
	 * @param minY minimum height
	 * @param maxY maximum height.
	 * @return new BCLFeature instance.
	 */
	public static BCLFeature makeOreFeature(ResourceLocation id, Block blockOre, Block hostBlock, int veins, int veinSize, float airDiscardChance, int minY, int maxY) {
		return makeOreFeature(id, blockOre, hostBlock, veins, veinSize, airDiscardChance, VerticalAnchor.absolute(minY), VerticalAnchor.absolute(maxY), false);
	}
	
	/**
	 * Will create a basic ore feature.
	 * @param id {@link ResourceLocation} feature ID.
	 * @param blockOre {@link Decoration} feature step.
	 * @param hostBlock {@link Block} to generate feature in.
	 * @param veins iterations per chunk.
	 * @param veinSize size of ore vein.
	 * @param airDiscardChance chance that this orge gets discarded when it is exposed to air
	 * @param minY A {@link VerticalAnchor} for the minimum height, for example
	 *				{@code VerticalAnchor.bottom()}, {@code VerticalAnchor.absolute(10)}, {@code VerticalAnchor.aboveBottom(10)}
	 * @param maxY A {@link VerticalAnchor} for the maximum height.
	 * @return new BCLFeature instance.
	 */
	public static BCLFeature makeOreFeature(ResourceLocation id, Block blockOre, Block hostBlock, int veins, int veinSize, float airDiscardChance, VerticalAnchor minY, VerticalAnchor maxY) {
		return makeOreFeature(id, blockOre, hostBlock, veins, veinSize, airDiscardChance, minY, maxY, false);
	}
	
	/**
	 * Will create a basic ore feature.
	 * @param id {@link ResourceLocation} feature ID.
	 * @param blockOre {@link Decoration} feature step.
	 * @param hostBlock {@link Block} to generate feature in.
	 * @param veins iterations per chunk.
	 * @param veinSize size of ore vein.
	 * @param minY A {@link VerticalAnchor} for the minimum height, for example
	 *				{@code VerticalAnchor.bottom()}, {@code VerticalAnchor.absolute(10)}, {@code VerticalAnchor.aboveBottom(10)}
	 * @param maxY A {@link VerticalAnchor} for the maximum height.
	 * @return new BCLFeature instance.
	 */
	public static BCLFeature makeOreFeature(ResourceLocation id, Block blockOre, Block hostBlock, int veins, int veinSize, VerticalAnchor minY, VerticalAnchor maxY) {
		return makeOreFeature(id, blockOre, hostBlock, veins, veinSize, 0.0f, minY, maxY, false);
	}
	
	/**
	 * Will create a basic ore feature.
	 *
	 * @param id		{@link ResourceLocation} feature ID.
	 * @param blockOre  {@link Decoration} feature step.
	 * @param hostBlock {@link Block} to generate feature in.
	 * @param veins	 iterations per chunk.
	 * @param veinSize  size of ore vein.
	 * @param airDiscardChance chance that this orge gets discarded when it is exposed to air
	 * @param minY A {@link VerticalAnchor} for the minimum height, for example
	 *				{@code VerticalAnchor.bottom()}, {@code VerticalAnchor.absolute(10)}, {@code VerticalAnchor.aboveBottom(10)}
	 * @param maxY A {@link VerticalAnchor} for the maximum height.
	 * @param rare	  when true, this is placed as a rare resource
	 * @return new BCLFeature instance.
	 */
	public static BCLFeature makeOreFeature(ResourceLocation id, Block blockOre, Block hostBlock, int veins, int veinSize, float airDiscardChance, VerticalAnchor minY, VerticalAnchor maxY, boolean rare) {
		return makeOreFeature(id, blockOre, hostBlock, veins, veinSize, airDiscardChance, HeightRangePlacement.uniform(minY, maxY), rare);
	}
	
	@Deprecated(forRemoval = true)
	/**
	 * Will create a basic ore feature.
	 *
	 * @param id		{@link ResourceLocation} feature ID.
	 * @param blockOre  {@link Decoration} feature step.
	 * @param hostBlock {@link Block} to generate feature in.
	 * @param veins	 iterations per chunk.
	 * @param veinSize  size of ore vein.
	 * @param minY A {@link VerticalAnchor} for the minimum height, for example
	 *				{@code VerticalAnchor.bottom()}, {@code VerticalAnchor.absolute(10)}, {@code VerticalAnchor.aboveBottom(10)}
	 * @param maxY A {@link VerticalAnchor} for the maximum height.
	 * @param rare	  when true, this is placed as a rare resource
	 * @return new BCLFeature instance.
	 */
	public static BCLFeature makeOreFeature(ResourceLocation id, Block blockOre, Block hostBlock, int veins, int veinSize, VerticalAnchor minY, VerticalAnchor maxY, boolean rare) {
		return makeOreFeature(id, blockOre, hostBlock, veins, veinSize, 0.0f, HeightRangePlacement.uniform(minY, maxY), rare);
	}
	
	/**
	 * Will create a basic ore feature.
	 *
	 * @param id		{@link ResourceLocation} feature ID.
	 * @param blockOre  {@link Decoration} feature step.
	 * @param hostBlock {@link Block} to generate feature in.
	 * @param veins	 iterations per chunk.
	 * @param veinSize  size of ore vein.
	 * @param airDiscardChance chance that this orge gets discarded when it is exposed to air
	 * @param placement {@link net.minecraft.world.level.levelgen.placement.PlacementModifier} for the ore distribution,
	 *				  for example {@code PlacementUtils.FULL_RANGE}, {@code PlacementUtils.RANGE_10_10}
	 * @param rare	  when true, this is placed as a rare resource
	 * @return new BCLFeature instance.
	 */
	public static BCLFeature makeOreFeature(ResourceLocation id, Block blockOre, Block hostBlock, int veins, int veinSize, float airDiscardChance, PlacementModifier placement, boolean rare) {
		OreConfiguration featureConfig = new OreConfiguration(
			new BlockMatchTest(hostBlock),
			blockOre.defaultBlockState(),
			veinSize,
			airDiscardChance
		);
		
		PlacedFeature oreFeature = Feature.ORE
			.configured(featureConfig)
			.placed(
				InSquarePlacement.spread(),
				rare ? RarityFilter.onAverageOnceEvery(veins) : CountPlacement.of(veins),
				placement,
				BiomeFilter.biome()
			);
		
		return new BCLFeature(
			net.minecraft.world.level.levelgen.feature.Feature.ORE,
			Registry.register(BuiltinRegistries.PLACED_FEATURE, id, oreFeature),
			Decoration.UNDERGROUND_ORES
		);
	}
	
	
	@Deprecated(forRemoval = true)
	/**
	 * Will create a basic ore feature.
	 *
	 * @param id		{@link ResourceLocation} feature ID.
	 * @param blockOre  {@link Decoration} feature step.
	 * @param hostBlock {@link Block} to generate feature in.
	 * @param veins	 iterations per chunk.
	 * @param veinSize  size of ore vein.
	 * @param placement {@link net.minecraft.world.level.levelgen.placement.PlacementModifier} for the ore distribution,
	 *				  for example {@code PlacementUtils.FULL_RANGE}, {@code PlacementUtils.RANGE_10_10}
	 * @param rare	  when true, this is placed as a rare resource
	 * @return new BCLFeature instance.
	 */
	public static BCLFeature makeOreFeature(ResourceLocation id, Block blockOre, Block hostBlock, int veins, int veinSize,  PlacementModifier placement, boolean rare) {
		return makeOreFeature(id, blockOre, hostBlock, veins, veinSize, 0.0f, placement, rare);
	}
	
	/**
	 * Will create feature which will be generated once in each chunk.
	 * @param id {@link ResourceLocation} feature ID.
	 * @param step {@link Decoration} feature step.
	 * @param feature {@link Feature} with {@link NoneFeatureConfiguration} config.
	 * @return new BCLFeature instance.
	 */
	public static BCLFeature makeChunkFeature(ResourceLocation id, Decoration step, Feature<NoneFeatureConfiguration> feature) {
		return makeFeature(id, step, feature, CountPlacement.of(1));
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
		return makeFeature(id, step, feature, RarityFilter.onAverageOnceEvery(chance));
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
		return makeFeature(id, step, feature, CountPlacement.of(count));
	}
	
	/**
	 * Makes simple configured feature with {@link NoneFeatureConfiguration} set to NONE.
	 * @param id {@link ResourceLocation} feature ID.
	 * @param step {@link Decoration} feature step.
	 * @param feature {@link Feature} with {@link NoneFeatureConfiguration} config.
	 * @return new BCLFeature instance.
	 */
	public static BCLFeature makeFeatureConfigured(ResourceLocation id, Decoration step, Feature<NoneFeatureConfiguration> feature) {
		return makeFeature(id, step, feature);
	}
	
	/**
	 * Creates and configures new BCLib feature.
	 * @param id {@link ResourceLocation} feature ID.
	 * @param step {@link Decoration} feature step.
	 * @param feature {@link Feature} with {@link NoneFeatureConfiguration} config.
	 * @param placementModifiers array of {@link PlacementModifier}
	 * @return new BCLFeature instance.
	 */
	public static BCLFeature makeFeature(ResourceLocation id, Decoration step, Feature<NoneFeatureConfiguration> feature, PlacementModifier... placementModifiers) {
		PlacedFeature configured = feature.configured(FeatureConfiguration.NONE).placed(placementModifiers);
		return new BCLFeature(id, feature, step, configured);
	}
}
