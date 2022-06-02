package org.betterx.bclib.world.features.placement;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.levelgen.placement.PlacementContext;
import net.minecraft.world.level.levelgen.placement.PlacementModifier;
import net.minecraft.world.level.levelgen.placement.PlacementModifierType;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import java.util.stream.Stream;

public class Offset extends PlacementModifier {
    public static final Codec<Offset> CODEC = RecordCodecBuilder.create((instance) -> instance
            .group(
                    Vec3i.CODEC
                            .fieldOf("blocks")
                            .forGetter(cfg -> cfg.offset)
            )
            .apply(instance, Offset::new));

    private final Vec3i offset;

    public Offset(Vec3i offset) {
        this.offset = offset;
    }

    @Override
    public Stream<BlockPos> getPositions(PlacementContext placementContext,
                                         RandomSource randomSource,
                                         BlockPos blockPos) {
        return Stream.of(blockPos.offset(offset));
    }

    @Override
    public PlacementModifierType<?> type() {
        return PlacementModifiers.OFFSET;
    }
}
