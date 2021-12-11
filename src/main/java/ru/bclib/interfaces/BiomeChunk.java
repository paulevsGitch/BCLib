package ru.bclib.interfaces;

import ru.bclib.world.biomes.BCLBiome;

public interface BiomeChunk {
	void setBiome(int x, int z, BCLBiome biome);
	BCLBiome getBiome(int x, int z);
	int getSide();
}
