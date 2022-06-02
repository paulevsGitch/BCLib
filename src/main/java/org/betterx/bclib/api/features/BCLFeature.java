package org.betterx.bclib.api.features;

import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.data.BuiltinRegistries;
import net.minecraft.data.worldgen.features.FeatureUtils;
import net.minecraft.data.worldgen.placement.PlacementUtils;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.levelgen.GenerationStep.Decoration;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.configurations.FeatureConfiguration;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;
import net.minecraft.world.level.levelgen.placement.PlacementModifier;

import org.betterx.bclib.BCLib;
import org.betterx.bclib.api.features.config.BlockPlaceFeatureConfig;
import org.betterx.bclib.api.features.config.ScatterFeatureConfig;

import java.util.Map.Entry;
import java.util.Optional;

public class BCLFeature {
    public static final Feature<ScatterFeatureConfig.OnSolid> SCATTER_ON_SOLID = register(
            BCLib.makeID("scatter_on_solid"),
            new ScatterFeature<>(ScatterFeatureConfig.OnSolid.CODEC)
    );
    public static final Feature<BlockPlaceFeatureConfig> PLACE_BLOCK = register(
            BCLib.makeID("place_block"),
            new BlockPlaceFeature<>(BlockPlaceFeatureConfig.CODEC)
    );
    private final Holder<PlacedFeature> placedFeature;
    private final Decoration featureStep;
    private final Feature<?> feature;


    public <FC extends FeatureConfiguration, F extends Feature<FC>> BCLFeature(ResourceLocation id,
                                                                               F feature,
                                                                               Decoration featureStep,
                                                                               FC configuration,
                                                                               PlacementModifier[] modifiers) {
        this(id, feature, featureStep, buildPlacedFeature(id, feature, configuration, modifiers));
    }

    public BCLFeature(ResourceLocation id,
                      Feature<?> feature,
                      Decoration featureStep,
                      Holder<PlacedFeature> placedFeature) {
        this.placedFeature = placedFeature;
        this.featureStep = featureStep;
        this.feature = feature;

        if (!BuiltinRegistries.PLACED_FEATURE.containsKey(id)) {
            Registry.register(BuiltinRegistries.PLACED_FEATURE, id, placedFeature.value());
        }
        if (!Registry.FEATURE.containsKey(id) && !containsObj(Registry.FEATURE, feature)) {
            Registry.register(Registry.FEATURE, id, feature);
        }
    }

    private static <FC extends FeatureConfiguration, F extends Feature<FC>> Holder<PlacedFeature> buildPlacedFeature(
            ResourceLocation id,
            F feature,
            FC configuration,
            PlacementModifier[] modifiers) {
        Holder<ConfiguredFeature<?, ?>> configuredFeature;
        if (!BuiltinRegistries.CONFIGURED_FEATURE.containsKey(id)) {
            configuredFeature = (Holder<ConfiguredFeature<?, ?>>) (Object) FeatureUtils.register(id.toString(),
                    feature,
                    configuration);
        } else {
            configuredFeature = BuiltinRegistries.CONFIGURED_FEATURE
                    .getHolder(ResourceKey.create(BuiltinRegistries.CONFIGURED_FEATURE.key(),
                            id))
                    .orElseThrow();
        }

        if (!BuiltinRegistries.PLACED_FEATURE.containsKey(id)) {
            return PlacementUtils.register(id.toString(), configuredFeature, modifiers);
        } else {
            return BuiltinRegistries.PLACED_FEATURE.getHolder(ResourceKey.create(BuiltinRegistries.PLACED_FEATURE.key(),
                    id)).orElseThrow();
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

    public static <C extends FeatureConfiguration, F extends Feature<C>> F register(ResourceLocation string,
                                                                                    F feature) {
        return Registry.register(Registry.FEATURE, string, feature);
    }

    /**
     * Get raw feature.
     *
     * @return {@link Feature}.
     */
    public Feature<?> getFeature() {
        return feature;
    }

    /**
     * Get configured feature.
     *
     * @return {@link PlacedFeature}.
     */
    public Holder<PlacedFeature> getPlacedFeature() {
        return placedFeature;
    }

    /**
     * Get feature decoration step.
     *
     * @return {@link Decoration}.
     */
    public Decoration getDecoration() {
        return featureStep;
    }
}
