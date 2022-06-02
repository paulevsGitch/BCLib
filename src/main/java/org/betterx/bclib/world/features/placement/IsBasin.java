package org.betterx.bclib.world.features.placement;

import net.minecraft.core.BlockPos;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.placement.PlacementContext;
import net.minecraft.world.level.levelgen.placement.PlacementFilter;
import net.minecraft.world.level.levelgen.placement.PlacementModifierType;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import java.util.List;

public class IsBasin extends PlacementFilter {
    public static final Codec<IsBasin> CODEC = RecordCodecBuilder.create((instance) -> instance
            .group(
                    ExtraCodecs.nonEmptyList(BlockState.CODEC.listOf())
                               .fieldOf("blocks")
                               .forGetter(cfg -> cfg.blocks)
            )
            .apply(instance, IsBasin::new));

    private final List<BlockState> blocks;

    public IsBasin(List<BlockState> blocks) {
        this.blocks = blocks;
    }

    @Override
    protected boolean shouldPlace(PlacementContext ctx, RandomSource random, BlockPos pos) {
        BlockState bs = ctx.getLevel().getBlockState(pos);
        return blocks.stream().map(b -> b.getBlock()).anyMatch(b -> bs.is(b));
    }

    @Override
    public PlacementModifierType<?> type() {
        return PlacementModifiers.IS_BASIN;
    }
}
