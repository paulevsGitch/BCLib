package org.betterx.bclib.world.features;

import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;

import com.mojang.serialization.Codec;
import org.betterx.bclib.util.BlocksHelper;

import java.util.Optional;

public class BlockPlaceFeature<FC extends BlockPlaceFeatureConfig> extends Feature<FC> {
    public BlockPlaceFeature(Codec<FC> codec) {
        super(codec);
    }

    @Override
    public boolean place(FeaturePlaceContext<FC> ctx) {
        Optional<BlockState> state = ctx.config().getRandomBlock(ctx.random());
        if (state.isPresent())
            BlocksHelper.setWithoutUpdate(ctx.level(), ctx.origin(), state.get());
        return true;
    }
}
