package ru.bclib.api;

import java.util.Map;
import java.util.Random;
import java.util.Set;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import ru.bclib.util.WeightedList;

public class BonemealAPI {
	private static final Map<ResourceLocation, Map<Block, WeightedList<Block>>> WATER_GRASS_BIOMES = Maps.newHashMap();
	private static final Map<ResourceLocation, Map<Block, WeightedList<Block>>> LAND_GRASS_BIOMES = Maps.newHashMap();
	private static final Map<Block, WeightedList<Block>> WATER_GRASS_TYPES = Maps.newHashMap();
	private static final Map<Block, WeightedList<Block>> LAND_GRASS_TYPES = Maps.newHashMap();
	private static final Set<Block> SPREADABLE_BLOCKS = Sets.newHashSet();
	
	public static void addSpreadableBlock(Block block) {
		SPREADABLE_BLOCKS.add(block);
	}
	
	public static boolean isSpreadable(Block block) {
		return SPREADABLE_BLOCKS.contains(block);
	}
	
	public static void addLandGrass(Block plant, Block... terrain) {
		for (Block block: terrain) {
			addLandGrass(block, plant, 1F);
		}
	}
	
	public static void addLandGrass(ResourceLocation biome, Block plant, Block... terrain) {
		for (Block block: terrain) {
			addLandGrass(biome, block, plant, 1F);
		}
	}
	
	public static void addLandGrass(Block terrain, Block plant, float chance) {
		WeightedList<Block> list = LAND_GRASS_TYPES.get(terrain);
		if (list == null) {
			list = new WeightedList<Block>();
			LAND_GRASS_TYPES.put(terrain, list);
		}
		list.add(plant, chance);
	}
	
	public static void addLandGrass(ResourceLocation biome, Block terrain, Block plant, float chance) {
		Map<Block, WeightedList<Block>> map = LAND_GRASS_BIOMES.get(biome);
		if (map == null) {
			map = Maps.newHashMap();
			LAND_GRASS_BIOMES.put(biome, map);
		}
		WeightedList<Block> list = map.get(terrain);
		if (list == null) {
			list = new WeightedList<Block>();
			map.put(terrain, list);
		}
		list.add(plant, chance);
	}
	
	public static void addWaterGrass(Block plant, Block... terrain) {
		for (Block block: terrain) {
			addWaterGrass(block, plant, 1F);
		}
	}
	
	public static void addWaterGrass(ResourceLocation biome, Block plant, Block... terrain) {
		for (Block block: terrain) {
			addWaterGrass(biome, block, plant, 1F);
		}
	}
	
	public static void addWaterGrass(Block terrain, Block plant, float chance) {
		WeightedList<Block> list = WATER_GRASS_TYPES.get(terrain);
		if (list == null) {
			list = new WeightedList<Block>();
			WATER_GRASS_TYPES.put(terrain, list);
		}
		list.add(plant, chance);
	}
	
	public static void addWaterGrass(ResourceLocation biome, Block terrain, Block plant, float chance) {
		Map<Block, WeightedList<Block>> map = WATER_GRASS_BIOMES.get(biome);
		if (map == null) {
			map = Maps.newHashMap();
			WATER_GRASS_BIOMES.put(biome, map);
		}
		WeightedList<Block> list = map.get(terrain);
		if (list == null) {
			list = new WeightedList<Block>();
			map.put(terrain, list);
		}
		list.add(plant, chance);
	}
	
	public static Block getLandGrass(ResourceLocation biomeID, Block terrain, Random random) {
		Map<Block, WeightedList<Block>> map = LAND_GRASS_BIOMES.get(biomeID);
		WeightedList<Block> list = null;
		if (map != null) {
			list = map.get(terrain);
			if (list == null) {
				list = LAND_GRASS_TYPES.get(terrain);
			}
		}
		else {
			list = LAND_GRASS_TYPES.get(terrain);
		}
		return list == null ? null : list.get(random);
	}
	
	public static Block getWaterGrass(ResourceLocation biomeID, Block terrain, Random random) {
		Map<Block, WeightedList<Block>> map = LAND_GRASS_BIOMES.get(biomeID);
		WeightedList<Block> list = null;
		if (map != null) {
			list = map.get(terrain);
			if (list == null) {
				list = LAND_GRASS_TYPES.get(terrain);
			}
		}
		else {
			list = LAND_GRASS_TYPES.get(terrain);
		}
		return list == null ? null : list.get(random);
	}
}
