package org.betterx.bclib.world.features;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.FeatureConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;

import com.mojang.serialization.Codec;
import org.betterx.bclib.util.BlocksHelper;

import java.util.Optional;

public abstract class SurfaceFeature<T extends FeatureConfiguration> extends Feature<T> {
    public static abstract class DefaultConfiguration extends SurfaceFeature<NoneFeatureConfiguration> {
        protected DefaultConfiguration() {
            super(NoneFeatureConfiguration.CODEC);
        }
    }

    protected SurfaceFeature(Codec<T> codec) {
        super(codec);
    }

    protected abstract boolean isValidSurface(BlockState state);

    protected int minHeight(FeaturePlaceContext<T> ctx) {
        return ctx.chunkGenerator().getSeaLevel();
    }

    @Override
    public boolean place(FeaturePlaceContext<T> ctx) {
        Optional<BlockPos> pos = BlocksHelper.findSurfaceBelow(ctx.level(),
                ctx.origin(),
                minHeight(ctx),
                this::isValidSurface);
        if (pos.isPresent()) {
            int y2 = ctx.level().getHeight(Heightmap.Types.WORLD_SURFACE_WG, ctx.origin().getX(), ctx.origin().getZ());
            int y3 = ctx.level().getHeight(Heightmap.Types.WORLD_SURFACE, ctx.origin().getX(), ctx.origin().getZ());
            System.out.println("Surfaces:" + pos.get().getY() + ", " + y2 + ", " + y3 + ", " + ctx.origin().getY());

            generate(pos.get(), ctx);
            return true;
        }


        return false;
    }

    protected abstract void generate(BlockPos centerPos, FeaturePlaceContext<T> ctx);
}
