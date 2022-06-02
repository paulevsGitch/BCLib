package org.betterx.bclib.world.features.placement;

import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.levelgen.blockpredicates.BlockPredicate;
import net.minecraft.world.level.levelgen.placement.PlacementContext;
import net.minecraft.world.level.levelgen.placement.PlacementFilter;
import net.minecraft.world.level.levelgen.placement.PlacementModifierType;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

public class Is extends PlacementFilter {
    public static final Codec<Is> CODEC = RecordCodecBuilder.create((instance) -> instance
            .group(
                    BlockPredicate.CODEC
                            .fieldOf("predicate")
                            .forGetter(cfg -> cfg.predicate)
            )
            .apply(instance, Is::new));

    private final BlockPredicate predicate;

    public Is(BlockPredicate predicate) {
        this.predicate = predicate;
    }

    public static Is simple(BlockPredicate predicate) {
        return new Is(predicate);
    }

    @Override
    protected boolean shouldPlace(PlacementContext ctx, RandomSource random, BlockPos pos) {
        WorldGenLevel level = ctx.getLevel();
        return predicate.test(level, pos);
    }

    @Override
    public PlacementModifierType<Is> type() {
        return PlacementModifiers.IS;
    }
}
