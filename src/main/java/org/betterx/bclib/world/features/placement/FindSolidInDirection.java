package org.betterx.bclib.world.features.placement;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.levelgen.placement.PlacementContext;
import net.minecraft.world.level.levelgen.placement.PlacementModifier;
import net.minecraft.world.level.levelgen.placement.PlacementModifierType;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import org.betterx.bclib.api.tag.CommonBlockTags;
import org.betterx.bclib.util.BlocksHelper;

import java.util.stream.Stream;

public class FindSolidInDirection extends PlacementModifier {

    protected static final FindSolidInDirection DOWN = new FindSolidInDirection(Direction.DOWN, 6);
    protected static final FindSolidInDirection UP = new FindSolidInDirection(Direction.UP, 6);
    public static final Codec<FindSolidInDirection> CODEC = RecordCodecBuilder.create((instance) -> instance
            .group(
                    Direction.CODEC.fieldOf("dir").orElse(Direction.DOWN).forGetter((p) -> p.direction),
                    Codec.intRange(1, 32).fieldOf("dist").orElse(12).forGetter((p) -> p.maxSearchDistance)
            )
            .apply(instance, FindSolidInDirection::new));

    public FindSolidInDirection(Direction direction, int maxSearchDistance) {
        this.direction = direction;
        this.maxSearchDistance = maxSearchDistance;
    }

    public static PlacementModifier down() {
        return DOWN;
    }

    public static PlacementModifier up() {
        return UP;
    }

    public static PlacementModifier down(int dist) {
        if (dist == DOWN.maxSearchDistance) return DOWN;
        return new FindSolidInDirection(Direction.DOWN, dist);
    }

    public static PlacementModifier up(int dist) {
        if (dist == UP.maxSearchDistance) return UP;
        return new FindSolidInDirection(Direction.UP, dist);
    }


    @Override
    public Stream<BlockPos> getPositions(PlacementContext placementContext,
                                         RandomSource randomSource,
                                         BlockPos blockPos) {
        BlockPos.MutableBlockPos POS = blockPos.mutable();
        if (BlocksHelper.findSurroundingSurface(placementContext.getLevel(),
                POS,
                direction,
                maxSearchDistance,
                state -> state.is(CommonBlockTags.TERRAIN))
        ) return Stream.of(POS);

        return Stream.of();
    }

    private final Direction direction;
    private final int maxSearchDistance;

    @Override
    public PlacementModifierType<?> type() {
        return PlacementModifiers.SOLID_IN_DIR;
    }
}
