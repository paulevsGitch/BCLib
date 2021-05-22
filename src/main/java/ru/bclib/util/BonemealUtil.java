package ru.bclib.util;

import java.util.Map;
import java.util.Random;

import com.google.common.collect.Maps;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;

public class BonemealUtil {
	private static final Map<ResourceLocation, Map<Block, WeightedList<Block>>> GRASS_BIOMES = Maps.newHashMap();
	private static final Map<Block, WeightedList<Block>> GRASS_TYPES = Maps.newHashMap();
	
	public static void addBonemealGrass(Block terrain, Block plant) {
		addBonemealGrass(terrain, plant, 1F);
	}
	
	public static void addBonemealGrass(Block terrain, Block plant, float chance) {
		WeightedList<Block> list = GRASS_TYPES.get(terrain);
		if (list == null) {
			list = new WeightedList<Block>();
			GRASS_TYPES.put(terrain, list);
		}
		list.add(plant, chance);
	}
	
	public static void addBonemealGrass(ResourceLocation biome, Block terrain, Block plant) {
		addBonemealGrass(biome, terrain, plant, 1F);
	}
	
	public static void addBonemealGrass(ResourceLocation biome, Block terrain, Block plant, float chance) {
		Map<Block, WeightedList<Block>> map = GRASS_BIOMES.get(biome);
		if (map == null) {
			map = Maps.newHashMap();
			GRASS_BIOMES.put(biome, map);
		}
		WeightedList<Block> list = map.get(terrain);
		if (list == null) {
			list = new WeightedList<Block>();
			map.put(terrain, list);
		}
		list.add(plant, chance);
	}
	
	public static Block getGrass(ResourceLocation biomeID, Block terrain, Random random) {
		Map<Block, WeightedList<Block>> map = GRASS_BIOMES.get(biomeID);
		WeightedList<Block> list = null;
		if (map != null) {
			list = map.get(terrain);
			if (list == null) {
				list = GRASS_TYPES.get(terrain);
			}
		}
		else {
			list = GRASS_TYPES.get(terrain);
		}
		return list == null ? null : list.get(random);
	}
}
