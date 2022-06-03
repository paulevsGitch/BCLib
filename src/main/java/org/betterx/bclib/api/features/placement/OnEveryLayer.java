package org.betterx.bclib.api.features.placement;

import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.placement.PlacementContext;
import net.minecraft.world.level.levelgen.placement.PlacementModifier;
import net.minecraft.world.level.levelgen.placement.PlacementModifierType;

import com.mojang.serialization.Codec;
import org.betterx.bclib.util.BlocksHelper;

import java.util.stream.Stream;

public class OnEveryLayer
        extends PlacementModifier {
    private static OnEveryLayer INSTANCE = new OnEveryLayer();
    public static final Codec<OnEveryLayer> CODEC = Codec.unit(() -> INSTANCE);


    private OnEveryLayer() {

    }

    public static OnEveryLayer simple() {
        return INSTANCE;
    }

    @Override
    public Stream<BlockPos> getPositions(PlacementContext ctx,
                                         RandomSource random,
                                         BlockPos pos) {

        Stream.Builder<BlockPos> builder = Stream.builder();

        final int z = pos.getZ();
        final int x = pos.getX();
        int y = ctx.getHeight(Heightmap.Types.MOTION_BLOCKING, x, z);
        int layerY;
        do {
            layerY = OnEveryLayer.findOnGroundYPosition(ctx, x, y, z);
            if (layerY != Integer.MAX_VALUE) {
                builder.add(new BlockPos(x, layerY, z));
                y = layerY - 1;
            }

        } while (layerY != Integer.MAX_VALUE);
        return builder.build();
    }

    @Override
    public PlacementModifierType<OnEveryLayer> type() {
        return PlacementModifiers.ON_EVERY_LAYER;
    }

    private static int findOnGroundYPosition(PlacementContext ctx, int x, int startY, int z) {
        BlockPos.MutableBlockPos mPos = new BlockPos.MutableBlockPos(x, startY, z);
        BlockState nowState = ctx.getBlockState(mPos);
        for (int y = startY; y >= ctx.getMinBuildHeight() + 1; --y) {
            mPos.setY(y - 1);
            BlockState belowState = ctx.getBlockState(mPos);
            if (BlocksHelper.isTerrain(belowState) && BlocksHelper.isFreeOrFluid(nowState) && !belowState.is(Blocks.BEDROCK)) {
                return mPos.getY() + 1;
            }
            nowState = belowState;
        }
        return Integer.MAX_VALUE;
    }
}
