package ru.bclib.interfaces;

import ru.bclib.world.biomes.BCLBiome;

public interface BiomeMap {
	void clearCache();
	
	BCLBiome getBiome(double x, double z);
}
