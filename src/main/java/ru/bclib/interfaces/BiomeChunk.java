package ru.bclib.interfaces;

import ru.bclib.world.biomes.BCLBiome;
import ru.bclib.world.generator.BiomePicker;

public interface BiomeChunk {
	void setBiome(int x, int z, BiomePicker.Entry biome);
	BiomePicker.Entry getBiome(int x, int z);
	int getSide();
}
