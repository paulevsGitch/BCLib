package ru.bclib.api.features;

import net.minecraft.data.worldgen.placement.PlacementUtils;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.valueproviders.UniformInt;
import net.minecraft.world.level.levelgen.GenerationStep.Decoration;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import net.minecraft.world.level.levelgen.placement.BiomeFilter;
import net.minecraft.world.level.levelgen.placement.CountOnEveryLayerPlacement;
import net.minecraft.world.level.levelgen.placement.CountPlacement;
import net.minecraft.world.level.levelgen.placement.InSquarePlacement;
import net.minecraft.world.level.levelgen.placement.PlacementModifier;
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
		return BCLFeatureBuilder.start(id, feature).build();
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
				.countAverage(density)
				.squarePlacement()
				.heightmap()
				.onlyInBiome()
				.build();
		}
	}
}
