package ru.bclib.world.generator;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import net.minecraft.resources.ResourceLocation;
import ru.bclib.util.WeighTree;
import ru.bclib.util.WeightedList;
import ru.bclib.world.biomes.BCLBiome;

import java.util.List;
import java.util.Random;
import java.util.Set;

public class BiomePicker {
	private final Set<ResourceLocation> immutableIDs = Sets.newHashSet();
	private final List<BCLBiome> biomes = Lists.newArrayList();
	private WeighTree<BCLBiome> tree;
	private int biomeCount = 0;
	
	public void addBiome(BCLBiome biome) {
		if (immutableIDs.contains(biome.getID())) {
			return;
		}
		immutableIDs.add(biome.getID());
		biomes.add(biome);
		biomeCount++;
	}
	
	public void addBiomeMutable(BCLBiome biome) {
		if (immutableIDs.contains(biome.getID())) {
			return;
		}
		biomes.add(biome);
	}
	
	public void clearMutables() {
		for (int i = biomes.size() - 1; i >= biomeCount; i--) {
			biomes.remove(i);
		}
	}
	
	public BCLBiome getBiome(Random random) {
		return biomes.isEmpty() ? null : tree.get(random);
	}
	
	public List<BCLBiome> getBiomes() {
		return biomes;
	}
	
	public boolean containsImmutable(ResourceLocation id) {
		return immutableIDs.contains(id);
	}
	
	public void removeMutableBiome(ResourceLocation id) {
		for (int i = biomeCount; i < biomes.size(); i++) {
			BCLBiome biome = biomes.get(i);
			if (biome.getID().equals(id)) {
				biomes.remove(i);
				break;
			}
		}
	}
	
	public void rebuild() {
		if (biomes.isEmpty()) {
			return;
		}
		WeightedList<BCLBiome> list = new WeightedList<>();
		biomes.forEach(biome -> {
			list.add(biome, biome.getGenChance());
		});
		tree = new WeighTree<>(list);
	}
}
