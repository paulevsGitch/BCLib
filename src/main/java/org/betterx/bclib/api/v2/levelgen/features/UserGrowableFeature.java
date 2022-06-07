package org.betterx.bclib.api.v2.levelgen.features;

import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.ServerLevelAccessor;

public interface UserGrowableFeature {
    boolean grow(ServerLevelAccessor level,
                 BlockPos pos,
                 RandomSource random);
}
