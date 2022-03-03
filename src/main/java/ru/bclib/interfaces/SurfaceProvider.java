package ru.bclib.interfaces;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.state.BlockState;

public interface SurfaceProvider {
	public BlockState bclib_getSurface(BlockPos pos, Biome biome, ServerLevel level);
}
