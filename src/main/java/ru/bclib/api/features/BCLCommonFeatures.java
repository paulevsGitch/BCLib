package ru.bclib.api.features;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.levelgen.GenerationStep.Decoration;
import net.minecraft.world.level.levelgen.VerticalAnchor;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.OreConfiguration;
import net.minecraft.world.level.levelgen.placement.HeightRangePlacement;
import net.minecraft.world.level.levelgen.placement.PlacementModifier;
import net.minecraft.world.level.levelgen.structure.templatesystem.BlockMatchTest;
import ru.bclib.world.features.BCLFeature;

public class BCLCommonFeatures {
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
			return BCLFeatureBuilder.start(id, feature).countLayers(density).onlyInBiome().build();
		}
		else {
			return BCLFeatureBuilder
				.start(id, feature)
				.oncePerChunks(density)
				.squarePlacement()
				.heightmap()
				.onlyInBiome()
				.build();
		}
	}
	
	/**
	 * Will create feature which will be generated once in each chunk.
	 * @param id {@link ResourceLocation} feature ID.
	 * @param decoration {@link Decoration} feature step.
	 * @param feature {@link Feature} with {@link NoneFeatureConfiguration} config.
	 * @return new BCLFeature instance.
	 */
	public static BCLFeature makeChunkFeature(ResourceLocation id, Decoration decoration, Feature<NoneFeatureConfiguration> feature) {
		return BCLFeatureBuilder.start(id, feature).decoration(decoration).count(1).onlyInBiome().build();
	}
	
	/**
	 * Will create feature with chanced decoration, chance for feature to generate per chunk is 1 / chance.
	 * @param id {@link ResourceLocation} feature ID.
	 * @param decoration {@link Decoration} feature step.
	 * @param feature {@link Feature} with {@link NoneFeatureConfiguration} config.
	 * @param chance chance for feature to be generated in.
	 * @return new BCLFeature instance.
	 */
	public static BCLFeature makeChancedFeature(ResourceLocation id, Decoration decoration, Feature<NoneFeatureConfiguration> feature, int chance) {
		return BCLFeatureBuilder.start(id, feature).decoration(decoration).oncePerChunks(chance).onlyInBiome().build();
	}
	
	/**
	 * Will create feature with specified generation iterations per chunk.
	 * @param id {@link ResourceLocation} feature ID.
	 * @param decoration {@link Decoration} feature step.
	 * @param feature {@link Feature} with {@link NoneFeatureConfiguration} config.
	 * @param count iterations steps.
	 * @return new BCLFeature instance.
	 */
	public static BCLFeature makeCountFeature(ResourceLocation id, Decoration decoration, Feature<NoneFeatureConfiguration> feature, int count) {
		return BCLFeatureBuilder.start(id, feature).decoration(decoration).count(count).onlyInBiome().build();
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
	 * for example {@code PlacementUtils.FULL_RANGE}, {@code PlacementUtils.RANGE_10_10}
	 * @param rare	  when true, this is placed as a rare resource
	 * @return new BCLFeature instance.
	 */
	public static BCLFeature makeOreFeature(ResourceLocation id, Block blockOre, Block hostBlock, int veins, int veinSize, float airDiscardChance, PlacementModifier placement, boolean rare) {
		BCLFeatureBuilder builder = BCLFeatureBuilder
			.start(id, Feature.ORE)
			.decoration(Decoration.UNDERGROUND_ORES)
			.modifier(placement)
			.squarePlacement()
			.onlyInBiome();
		
		if (rare) {
			builder.oncePerChunks(veins);
		}
		else {
			builder.oncePerChunks(veins);
		}
		
		return builder.build(new OreConfiguration(
			new BlockMatchTest(hostBlock),
			blockOre.defaultBlockState(),
			veinSize,
			airDiscardChance
		));
	}
	
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
	@Deprecated(forRemoval = true)
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
	 * @param minY A {@link VerticalAnchor} for the minimum height, for example
	 *				{@code VerticalAnchor.bottom()}, {@code VerticalAnchor.absolute(10)}, {@code VerticalAnchor.aboveBottom(10)}
	 * @param maxY A {@link VerticalAnchor} for the maximum height.
	 * @param rare	  when true, this is placed as a rare resource
	 * @return new BCLFeature instance.
	 */
	public static BCLFeature makeOreFeature(ResourceLocation id, Block blockOre, Block hostBlock, int veins, int veinSize, float airDiscardChance, VerticalAnchor minY, VerticalAnchor maxY, boolean rare) {
		return makeOreFeature(id, blockOre, hostBlock, veins, veinSize, airDiscardChance, HeightRangePlacement.uniform(minY, maxY), rare);
	}
	
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
	@Deprecated(forRemoval = true)
	public static BCLFeature makeOreFeature(ResourceLocation id, Block blockOre, Block hostBlock, int veins, int veinSize,  PlacementModifier placement, boolean rare) {
		return makeOreFeature(id, blockOre, hostBlock, veins, veinSize, 0.0f, placement, rare);
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
	 * @param minY minimum height
	 * @param maxY maximum height.
	 * @return new BCLFeature instance.
	 */
	public static BCLFeature makeOreFeature(ResourceLocation id, Block blockOre, Block hostBlock, int veins, int veinSize, int minY, int maxY) {
		return makeOreFeature(id, blockOre, hostBlock, veins, veinSize, VerticalAnchor.absolute(minY), VerticalAnchor.absolute(maxY), false);
	}
}
