package org.betterx.bclib.api.features;

import net.minecraft.data.worldgen.placement.PlacementUtils;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.valueproviders.UniformInt;
import net.minecraft.world.level.levelgen.GenerationStep.Decoration;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.configurations.FeatureConfiguration;
import net.minecraft.world.level.levelgen.placement.*;

import org.betterx.bclib.world.features.BCLFeature;

import java.util.ArrayList;
import java.util.List;

public class BCLFeatureBuilder<FC extends FeatureConfiguration, F extends Feature<FC>> {
    private static final BCLFeatureBuilder INSTANCE = new BCLFeatureBuilder();
    private final List<PlacementModifier> modifications = new ArrayList<>(16);
    private ResourceLocation featureID;
    private Decoration decoration;
    private F feature;

    private BCLFeatureBuilder() {
    }

    /**
     * Starts a new {@link BCLFeature} builder.
     *
     * @param featureID {@link ResourceLocation} feature identifier.
     * @param feature   {@link Feature} to construct.
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
     *
     * @param decoration {@link Decoration} step.
     * @return same {@link BCLFeatureBuilder} instance.
     */
    public BCLFeatureBuilder decoration(Decoration decoration) {
        this.decoration = decoration;
        return this;
    }

    /**
     * Add feature placement modifier. Used as a condition for feature how to generate.
     *
     * @param modifier {@link PlacementModifier}.
     * @return same {@link BCLFeatureBuilder} instance.
     */
    public BCLFeatureBuilder modifier(PlacementModifier modifier) {
        modifications.add(modifier);
        return this;
    }

    public BCLFeatureBuilder modifier(List<PlacementModifier> modifiers) {
        modifications.addAll(modifiers);
        return this;
    }

    /**
     * Generate feature in certain iterations (per chunk).
     *
     * @param count how many times feature will be generated in chunk.
     * @return same {@link BCLFeatureBuilder} instance.
     */
    public BCLFeatureBuilder count(int count) {
        return modifier(CountPlacement.of(count));
    }

    /**
     * Generate feature in certain iterations (per chunk). Count can be between 0 and max value.
     *
     * @param count maximum amount of iterations per chunk.
     * @return same {@link BCLFeatureBuilder} instance.
     */
    public BCLFeatureBuilder countMax(int count) {
        return modifier(CountPlacement.of(UniformInt.of(0, count)));
    }

    /**
     * Generate feature in certain iterations (per chunk).
     * Feature will be generated on all layers (example - Nether plants).
     *
     * @param count how many times feature will be generated in chunk layers.
     * @return same {@link BCLFeatureBuilder} instance.
     */
    @SuppressWarnings("deprecation")
    public BCLFeatureBuilder countLayers(int count) {
        return modifier(CountOnEveryLayerPlacement.of(count));
    }

    /**
     * Generate feature in certain iterations (per chunk). Count can be between 0 and max value.
     * Feature will be generated on all layers (example - Nether plants).
     *
     * @param count maximum amount of iterations per chunk layers.
     * @return same {@link BCLFeatureBuilder} instance.
     */
    @SuppressWarnings("deprecation")
    public BCLFeatureBuilder countLayersMax(int count) {
        return modifier(CountOnEveryLayerPlacement.of(UniformInt.of(0, count)));
    }

    /**
     * Will place feature once in certain amount of chunks (in average).
     *
     * @param chunks amount of chunks.
     * @return same {@link BCLFeatureBuilder} instance.
     */
    public BCLFeatureBuilder oncePerChunks(int chunks) {
        return modifier(RarityFilter.onAverageOnceEvery(chunks));
    }

    /**
     * Restricts feature generation only to biome where feature was added.
     *
     * @return same {@link BCLFeatureBuilder} instance.
     */
    public BCLFeatureBuilder onlyInBiome() {
        return modifier(BiomeFilter.biome());
    }

    public BCLFeatureBuilder squarePlacement() {
        return modifier(InSquarePlacement.spread());
    }

    public BCLFeatureBuilder heightmap() {
        return modifier(PlacementUtils.HEIGHTMAP);
    }

    /**
     * Builds a new {@link BCLFeature} instance. Features will be registered during this process.
     *
     * @param configuration any {@link FeatureConfiguration} for provided {@link Feature}.
     * @return created {@link BCLFeature} instance.
     */
    public BCLFeature build(FC configuration) {
        PlacementModifier[] modifiers = modifications.toArray(new PlacementModifier[modifications.size()]);
        return new BCLFeature(featureID, feature, decoration, configuration, modifiers);
    }

    /**
     * Builds a new {@link BCLFeature} instance with {@code NONE} {@link FeatureConfiguration}.
     * Features will be registered during this process.
     *
     * @return created {@link BCLFeature} instance.
     */
    public BCLFeature build() {
        return build((FC) FeatureConfiguration.NONE);
    }
}
