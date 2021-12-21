package ru.bclib.api.features;

import net.minecraft.data.worldgen.placement.PlacementUtils;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.levelgen.GenerationStep.Decoration;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.configurations.FeatureConfiguration;
import net.minecraft.world.level.levelgen.placement.BiomeFilter;
import net.minecraft.world.level.levelgen.placement.CountOnEveryLayerPlacement;
import net.minecraft.world.level.levelgen.placement.CountPlacement;
import net.minecraft.world.level.levelgen.placement.InSquarePlacement;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;
import net.minecraft.world.level.levelgen.placement.PlacementModifier;
import net.minecraft.world.level.levelgen.placement.RarityFilter;
import ru.bclib.world.features.BCLFeature;

import java.util.ArrayList;
import java.util.List;

public class BCLFeatureBuilder {
	private static final BCLFeatureBuilder INSTANCE = new BCLFeatureBuilder();
	private List<PlacementModifier> modifications = new ArrayList<>(16);
	private ResourceLocation featureID;
	private Decoration decoration;
	private Feature<?> feature;
	
	private BCLFeatureBuilder() {}
	
	/**
	 * Starts a new {@link BCLFeature} builder.
	 * @param featureID {@link ResourceLocation} feature identifier.
	 * @param feature {@link Feature} to construct.
	 * @return {@link BCLFeatureBuilder} instance.
	 */
	public static BCLFeatureBuilder start(ResourceLocation featureID, Feature<?> feature) {
		INSTANCE.decoration = Decoration.VEGETAL_DECORATION;
		INSTANCE.modifications.clear();
		INSTANCE.featureID = featureID;
		INSTANCE.feature = feature;
		return INSTANCE;
	}
	
	/**
	 * Set generation step for the feature. Default is {@code VEGETAL_DECORATION}.
	 * @param decoration {@link Decoration} step.
	 * @return same {@link BCLFeatureBuilder} instance.
	 */
	public BCLFeatureBuilder decoration(Decoration decoration) {
		this.decoration = decoration;
		return this;
	}
	
	/**
	 * Add feature placement modifier. Used as a condition for feature how to generate.
	 * @param modifier {@link PlacementModifier}.
	 * @return same {@link BCLFeatureBuilder} instance.
	 */
	public BCLFeatureBuilder modifier(PlacementModifier modifier) {
		modifications.add(modifier);
		return this;
	}
	
	/**
	 * Generate feature in certain iterations (per chunk).
	 * @param count how many times feature will be generated in chunk.
	 * @return same {@link BCLFeatureBuilder} instance.
	 */
	public BCLFeatureBuilder count(int count) {
		return modifier(CountPlacement.of(count));
	}
	
	/**
	 * Generate feature in certain iterations (per chunk), count can be different in different chunks.
	 * @param average how many times feature will be generated in chunk (in average).
	 * @return same {@link BCLFeatureBuilder} instance.
	 */
	public BCLFeatureBuilder countAverage(int average) {
		return modifier(RarityFilter.onAverageOnceEvery(average));
	}
	
	/**
	 * Generate feature in certain iterations (per chunk).
	 * Feature will be generated on all layers (example - Nether plants).
	 * @param count how many times feature will be generated in chunk layers.
	 * @return same {@link BCLFeatureBuilder} instance.
	 */
	@SuppressWarnings("deprecation")
	public BCLFeatureBuilder countLayers(int count) {
		return modifier(CountOnEveryLayerPlacement.of(count));
	}
	
	/**
	 * Will place feature once in certain amount of chunks (in average).
	 * @param chunks amount of chunks.
	 * @return same {@link BCLFeatureBuilder} instance.
	 */
	public BCLFeatureBuilder oncePerChunks(int chunks) {
		return modifier(RarityFilter.onAverageOnceEvery(chunks));
	}
	
	/**
	 * Restricts feature generation only to biome where feature was added.
	 * @return same {@link BCLFeatureBuilder} instance.
	 */
	public BCLFeatureBuilder onlyInBiome() {
		return modifier(BiomeFilter.biome());
	}
	
	// Are these two things required in 1.18.1?
	// TODO - add information
	public BCLFeatureBuilder squarePlacement() {
		return modifier(InSquarePlacement.spread());
	}
	
	// TODO - add information
	public BCLFeatureBuilder heightmap() {
		return modifier(PlacementUtils.HEIGHTMAP);
	}
	
	/**
	 * Builds a new {@link BCLFeature} instance. Features will be registered during this process.
	 * @param configuration any {@link FeatureConfiguration} for provided {@link Feature}.
	 * @return created {@link BCLFeature} instance.
	 */
	public <FC extends FeatureConfiguration> BCLFeature build(FC configuration) {
		PlacementModifier [] modifiers = modifications.toArray(new PlacementModifier [modifications.size()]);
		PlacedFeature configured = ((Feature<FC>) feature).configured(configuration).placed(modifiers);
		return new BCLFeature(featureID, feature, decoration, configured);
	}
	
	/**
	 * Builds a new {@link BCLFeature} instance with {@code NONE} {@link FeatureConfiguration}.
	 * Features will be registered during this process.
	 * @return created {@link BCLFeature} instance.
	 */
	public BCLFeature build() {
		return build(FeatureConfiguration.NONE);
	}
}
