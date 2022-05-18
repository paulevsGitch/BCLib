package ru.bclib.interfaces;

import ru.bclib.world.generator.BiomePicker;

public interface BiomeChunk {
	void setBiome(int x, int z, BiomePicker.ActualBiome biome);
	BiomePicker.ActualBiome getBiome(int x, int z);
	int getSide();
}
