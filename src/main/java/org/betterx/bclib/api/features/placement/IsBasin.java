package org.betterx.bclib.api.features.placement;

import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.levelgen.blockpredicates.BlockPredicate;
import net.minecraft.world.level.levelgen.placement.PlacementContext;
import net.minecraft.world.level.levelgen.placement.PlacementFilter;
import net.minecraft.world.level.levelgen.placement.PlacementModifierType;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

public class IsBasin extends PlacementFilter {
    public static final Codec<IsBasin> CODEC = RecordCodecBuilder.create((instance) -> instance
            .group(
                    BlockPredicate.CODEC
                            .fieldOf("predicate")
                            .forGetter(cfg -> cfg.predicate)
            )
            .apply(instance, IsBasin::new));

    private final BlockPredicate predicate;

    public IsBasin(BlockPredicate predicate) {
        this.predicate = predicate;
    }

    public static IsBasin simple(BlockPredicate predicate) {

        return new IsBasin(predicate);
    }

    @Override
    protected boolean shouldPlace(PlacementContext ctx, RandomSource random, BlockPos pos) {
        WorldGenLevel level = ctx.getLevel();
        return predicate.test(level, pos.below())
                && predicate.test(level, pos.west())
                && predicate.test(level, pos.east())
                && predicate.test(level, pos.north())
                && predicate.test(level, pos.south());
    }

    @Override
    public PlacementModifierType<?> type() {
        return PlacementModifiers.IS_BASIN;
    }
}
