package org.betterx.bclib.api.features;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.levelgen.blockpredicates.BlockPredicate;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.configurations.FeatureConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.RandomPatchConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.SimpleBlockConfiguration;
import net.minecraft.world.level.levelgen.feature.stateproviders.BlockStateProvider;

import org.betterx.bclib.api.features.config.ScatterFeatureConfig;

public class FastFeatures {


    public static BCLFeature vine(ResourceLocation location,
                                  boolean onFloor,
                                  boolean sparse,
                                  ScatterFeatureConfig.Builder builder) {
        return scatter(location, onFloor, sparse, builder, BCLFeature.SCATTER_ON_SOLID);
    }

    public static BCLFeature scatter(ResourceLocation location,
                                     boolean onFloor,
                                     boolean sparse,
                                     ScatterFeatureConfig.Builder builder,
                                     Feature scatterFeature) {
        BCLFeatureBuilder fBuilder = BCLFeatureBuilder.start(location, scatterFeature);
        if (onFloor) {
            fBuilder.findSolidFloor(3).isEmptyAbove2();
            builder.onFloor();
        } else {
            fBuilder.findSolidCeil(3).isEmptyBelow2();
            builder.onCeil();
        }
        if (sparse) {
            fBuilder.onceEvery(3);
        }

        return fBuilder
                .is(BlockPredicate.ONLY_IN_AIR_PREDICATE)
                .buildAndRegister(builder.build());
    }

    public static BCLFeature patch(ResourceLocation location, Block block) {
        return patch(location, block, 96, 7, 3);
    }

    public static BCLFeature
    patch(ResourceLocation location, Block block, int attempts, int xzSpread, int ySpread) {
        return patch(location,
                attempts,
                xzSpread,
                ySpread,
                Feature.SIMPLE_BLOCK,
                new SimpleBlockConfiguration(BlockStateProvider.simple(block)));
    }

    public static BCLFeature
    patch(ResourceLocation location, Feature<NoneFeatureConfiguration> feature) {
        return patch(location, 96, 7, 3, feature, FeatureConfiguration.NONE);
    }

    public static BCLFeature
    patch(ResourceLocation location,
          int attempts,
          int xzSpread,
          int ySpread,
          Feature<NoneFeatureConfiguration> feature) {
        return patch(location, attempts, xzSpread, ySpread, feature, FeatureConfiguration.NONE);
    }

    public static <FC extends FeatureConfiguration> BCLFeature
    patch(ResourceLocation location,
          int attempts,
          int xzSpread,
          int ySpread,
          Feature<FC> feature,
          FC config) {
        ResourceLocation patchLocation = new ResourceLocation(location.getNamespace(), location.getPath() + "_patch");
        final BCLFeature SINGLE = BCLFeatureBuilder
                .start(location, feature)
                .findSolidFloor(Math.min(12, ySpread))
                .is(BlockPredicate.ONLY_IN_AIR_PREDICATE)
                .buildAndRegister(config);

        return BCLFeatureBuilder
                .start(patchLocation, Feature.RANDOM_PATCH)
                .buildAndRegister(new RandomPatchConfiguration(attempts, xzSpread, ySpread, SINGLE.getPlacedFeature()));
    }
}
