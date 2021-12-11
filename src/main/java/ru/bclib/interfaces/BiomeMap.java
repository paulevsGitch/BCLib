package ru.bclib.interfaces;

import ru.bclib.world.biomes.BCLBiome;

public interface BiomeMap {
	void setChunkProcessor(TriConsumer<Integer, Integer, Integer> processor);
	BiomeChunk getChunk(int cx, int cz, boolean update);
	BCLBiome getBiome(double x, double y, double z);
	void clearCache();
}
