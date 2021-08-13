package ru.bclib.interfaces;

import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.biome.Biome;

import java.util.List;

public interface BiomeListProvider {
	List<ResourceKey<Biome>> getBiomes();
}
