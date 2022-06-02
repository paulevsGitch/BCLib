package org.betterx.bclib.world.features.placement;

import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.levelgen.placement.PlacementContext;
import net.minecraft.world.level.levelgen.placement.PlacementFilter;
import net.minecraft.world.level.levelgen.placement.PlacementModifierType;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

/**
 * Tests if there is air at two locations above the tested block position
 */
public class IsEmptyAboveSampledFilter extends PlacementFilter {
    private static final IsEmptyAboveSampledFilter DEFAULT = new IsEmptyAboveSampledFilter(4, 2);
    public static final Codec<IsEmptyAboveSampledFilter> CODEC = RecordCodecBuilder.create((instance) -> instance
            .group(
                    Codec.intRange(1, 32).fieldOf("d1").orElse(2).forGetter((p) -> p.distance1),
                    Codec.intRange(1, 32).fieldOf("d2").orElse(4).forGetter((p) -> p.distance1)
            )
            .apply(instance, IsEmptyAboveSampledFilter::new));

    public static PlacementFilter emptyAbove4() {
        return DEFAULT;
    }

    public IsEmptyAboveSampledFilter(int d1, int d2) {
        this.distance1 = d1;
        this.distance2 = d2;
    }

    private final int distance1;
    private final int distance2;


    @Override
    protected boolean shouldPlace(PlacementContext ctx, RandomSource random, BlockPos pos) {
        WorldGenLevel level = ctx.getLevel();
        if (level.isEmptyBlock(pos.above(distance1)) && level.isEmptyBlock(pos.above(distance2))) {
            return true;
        }
        return false;
    }

    @Override
    public PlacementModifierType<?> type() {
        return PlacementModifiers.IS_EMPTY_ABOVE_SAMPLED_FILTER;
    }
}
