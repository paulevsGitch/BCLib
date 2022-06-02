package org.betterx.bclib.world.features.placement;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.levelgen.placement.PlacementContext;
import net.minecraft.world.level.levelgen.placement.PlacementFilter;
import net.minecraft.world.level.levelgen.placement.PlacementModifier;
import net.minecraft.world.level.levelgen.placement.PlacementModifierType;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import org.betterx.bclib.util.BlocksHelper;

public class MinEmptyFilter extends PlacementFilter {
    private static MinEmptyFilter DOWN = new MinEmptyFilter(Direction.DOWN, 12);
    public static final Codec<MinEmptyFilter> CODEC = RecordCodecBuilder.create((instance) -> instance
            .group(
                    Direction.CODEC.fieldOf("dir").orElse(Direction.DOWN).forGetter((p) -> p.direction),
                    Codec.intRange(1, 32).fieldOf("dist").orElse(12).forGetter((p) -> p.maxSearchDistance)
            )
            .apply(instance, MinEmptyFilter::new));

    private final Direction direction;
    private final int maxSearchDistance;

    protected MinEmptyFilter(Direction direction, int maxSearchDistance) {
        this.direction = direction;
        this.maxSearchDistance = maxSearchDistance;
    }

    public PlacementModifier down() {
        return DOWN;
    }

    public PlacementModifier down(int dist) {
        return new MinEmptyFilter(Direction.DOWN, dist);
    }

    @Override
    protected boolean shouldPlace(PlacementContext ctx, RandomSource randomSource, BlockPos pos) {
        int h = BlocksHelper.blockCount(
                ctx.getLevel(),
                pos.relative(direction),
                direction,
                maxSearchDistance,
                state -> state.getMaterial().isReplaceable()
        );
        return false;
    }

    @Override
    public PlacementModifierType<?> type() {
        return PlacementModifiers.MIN_EMPTY_FILTER;
    }
}
