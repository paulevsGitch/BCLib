package org.betterx.bclib.world.features.placement;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.util.valueproviders.IntProvider;
import net.minecraft.util.valueproviders.UniformInt;
import net.minecraft.world.level.levelgen.placement.PlacementContext;
import net.minecraft.world.level.levelgen.placement.PlacementModifier;
import net.minecraft.world.level.levelgen.placement.PlacementModifierType;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

public class Extend extends PlacementModifier {
    public static final Codec<Extend> CODEC = RecordCodecBuilder.create((instance) -> instance
            .group(
                    Direction.CODEC
                            .fieldOf("direction")
                            .orElse(Direction.DOWN)
                            .forGetter(cfg -> cfg.direction),
                    IntProvider.codec(0, 16)
                               .fieldOf("length")
                               .orElse(UniformInt.of(0, 3))
                               .forGetter(cfg -> cfg.length)
            )
            .apply(instance, Extend::new));

    private final Direction direction;
    private final IntProvider length;

    public Extend(Direction direction, IntProvider length) {
        this.direction = direction;
        this.length = length;
    }

    @Override
    public Stream<BlockPos> getPositions(PlacementContext placementContext,
                                         RandomSource random,
                                         BlockPos blockPos) {
        final int count = length.sample(random);
        List<BlockPos> pos = new ArrayList<>(count);
        for (int y = 0; y < count; y++) {
            pos.add(blockPos.relative(direction, y + 1));
        }
        return pos.stream();
    }

    @Override
    public PlacementModifierType<?> type() {
        return PlacementModifiers.EXTEND;
    }
}