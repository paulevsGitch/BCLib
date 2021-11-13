package ru.bclib.interfaces;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.biome.Biome;

public interface BiomeSetter {
	public void bclib_setBiome(Biome biome, BlockPos pos);
}
