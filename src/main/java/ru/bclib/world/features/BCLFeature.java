package ru.bclib.world.features;

import net.minecraft.core.Registry;
import net.minecraft.data.BuiltinRegistries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.levelgen.GenerationStep.Decoration;
import net.minecraft.world.level.levelgen.VerticalAnchor;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.configurations.FeatureConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import net.minecraft.world.level.levelgen.placement.HeightRangePlacement;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;
import net.minecraft.world.level.levelgen.placement.PlacementModifier;
import ru.bclib.api.features.BCLCommonFeatures;

import java.util.Map.Entry;
import java.util.Optional;

public class BCLFeature {
	private PlacedFeature placedFeature;
	private Decoration featureStep;
	private Feature<?> feature;
	
	public BCLFeature(ResourceLocation id, Feature<?> feature, Decoration featureStep, PlacedFeature placedFeature) {
		this.placedFeature = placedFeature;
		this.featureStep = featureStep;
		this.feature = feature;
		
		if (!BuiltinRegistries.PLACED_FEATURE.containsKey(id)) {
			Registry.register(BuiltinRegistries.PLACED_FEATURE, id, placedFeature);
		}
		if (!Registry.FEATURE.containsKey(id) && !containsObj(Registry.FEATURE, feature)) {
			Registry.register(Registry.FEATURE, id, feature);
		}
	}
	
	private static <E> boolean containsObj(Registry<E> registry, E obj) {
		Optional<Entry<ResourceKey<E>, E>> optional = registry
			.entrySet()
			.stream()
			.filter(entry -> entry.getValue() == obj)
			.findAny();
		return optional.isPresent();
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
	 * Deprecated, use function from {@link BCLCommonFeatures} instead.
	 */
	@Deprecated(forRemoval = true)
	public static BCLFeature makeVegetationFeature(ResourceLocation id, Feature<NoneFeatureConfiguration> feature, int density) {
		return BCLCommonFeatures.makeVegetationFeature(id, feature, density);
	}
	
	/**
	 * Deprecated, use function from {@link BCLCommonFeatures} instead.
	 */
	@Deprecated(forRemoval = true)
	public static BCLFeature makeVegetationFeature(ResourceLocation id, Feature<NoneFeatureConfiguration> feature, int density, boolean allHeight) {
		return BCLCommonFeatures.makeVegetationFeature(id, feature, density, allHeight);
	}
	
	/**
	 * Deprecated, moved to {@link BCLCommonFeatures}. Will be completely removed.
	 */
	@Deprecated(forRemoval = true)
	public static BCLFeature makeOreFeature(ResourceLocation id, Block blockOre, Block hostBlock, int veins, int veinSize, float airDiscardChance, VerticalAnchor minY, VerticalAnchor maxY, boolean rare) {
		return BCLCommonFeatures.makeOreFeature(id, blockOre, hostBlock, veins, veinSize, airDiscardChance, HeightRangePlacement.uniform(minY, maxY), rare);
	}
	
	/**
	 * Deprecated, moved to {@link BCLCommonFeatures}. Will be completely removed.
	 */
	@Deprecated(forRemoval = true)
	public static BCLFeature makeOreFeature(ResourceLocation id, Block blockOre, Block hostBlock, int veins, int veinSize, VerticalAnchor minY, VerticalAnchor maxY, boolean rare) {
		return BCLCommonFeatures.makeOreFeature(id, blockOre, hostBlock, veins, veinSize, 0.0f, HeightRangePlacement.uniform(minY, maxY), rare);
	}
	
	/**
	 * Deprecated, use function from {@link BCLCommonFeatures} instead.
	 */
	@Deprecated(forRemoval = true)
	public static BCLFeature makeOreFeature(ResourceLocation id, Block blockOre, Block hostBlock, int veins, int veinSize, float airDiscardChance, PlacementModifier placement, boolean rare) {
		return BCLCommonFeatures.makeOreFeature(id, blockOre, hostBlock, veins, veinSize, airDiscardChance, placement, rare);
	}
	
	/**
	 * Deprecated, use function from {@link BCLCommonFeatures} instead.
	 */
	@Deprecated(forRemoval = true)
	public static BCLFeature makeOreFeature(ResourceLocation id, Block blockOre, Block hostBlock, int veins, int veinSize,  PlacementModifier placement, boolean rare) {
		return BCLCommonFeatures.makeOreFeature(id, blockOre, hostBlock, veins, veinSize, 0.0f, placement, rare);
	}
	
	/**
	 * Deprecated, use function from {@link BCLCommonFeatures} instead.
	 */
	@Deprecated(forRemoval = true)
	public static BCLFeature makeChunkFeature(ResourceLocation id, Decoration step, Feature<NoneFeatureConfiguration> feature) {
		return BCLCommonFeatures.makeChunkFeature(id, step, feature);
	}
	
	/**
	 * Deprecated, use function from {@link BCLCommonFeatures} instead.
	 */
	@Deprecated(forRemoval = true)
	public static BCLFeature makeChancedFeature(ResourceLocation id, Decoration step, Feature<NoneFeatureConfiguration> feature, int chance) {
		return BCLCommonFeatures.makeChancedFeature(id, step, feature, chance);
	}
	
	/**
	 * Deprecated, use function from {@link BCLCommonFeatures} instead.
	 */
	@Deprecated(forRemoval = true)
	public static BCLFeature makeCountFeature(ResourceLocation id, Decoration step, Feature<NoneFeatureConfiguration> feature, int count) {
		return BCLCommonFeatures.makeCountFeature(id, step, feature, count);
	}
	
	/**
	 * Deprecated, use {@link ru.bclib.api.features.BCLFeatureBuilder} instead.
	 *
	 * Creates and configures new BCLib feature.
	 * @param id {@link ResourceLocation} feature ID.
	 * @param step {@link Decoration} feature step.
	 * @param feature {@link Feature} with {@link NoneFeatureConfiguration} config.
	 * @param placementModifiers array of {@link PlacementModifier}
	 * @return new BCLFeature instance.
	 */
	@Deprecated(forRemoval = true)
	public static BCLFeature makeFeature(ResourceLocation id, Decoration step, Feature<NoneFeatureConfiguration> feature, PlacementModifier... placementModifiers) {
		PlacedFeature configured = feature.configured(FeatureConfiguration.NONE).placed(placementModifiers);
		return new BCLFeature(id, feature, step, configured);
	}
}
