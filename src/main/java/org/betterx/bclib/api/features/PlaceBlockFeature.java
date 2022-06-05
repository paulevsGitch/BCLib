package org.betterx.bclib.api.features;

import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;

import com.mojang.serialization.Codec;
import org.betterx.bclib.api.features.config.PlaceBlockFeatureConfig;

public class PlaceBlockFeature<FC extends PlaceBlockFeatureConfig> extends Feature<FC> {
    public PlaceBlockFeature(Codec<FC> codec) {
        super(codec);
    }

    @Override
    public boolean place(FeaturePlaceContext<FC> ctx) {
        return ctx.config().place(ctx);
    }
}
