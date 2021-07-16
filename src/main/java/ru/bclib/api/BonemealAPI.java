package ru.bclib.api;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import ru.bclib.util.WeightedList;

import java.util.Map;
import java.util.Random;
import java.util.Set;

public class BonemealAPI {
	private static final Map<ResourceLocation, Map<Block, WeightedList<Block>>> WATER_GRASS_BIOMES = Maps.newHashMap();
	private static final Map<ResourceLocation, Map<Block, WeightedList<Block>>> LAND_GRASS_BIOMES = Maps.newHashMap();
	private static final Map<Block, WeightedList<Block>> WATER_GRASS_TYPES = Maps.newHashMap();
	private static final Map<Block, WeightedList<Block>> LAND_GRASS_TYPES = Maps.newHashMap();
	private static final Map<Block, Block> SPREADABLE_BLOCKS = Maps.newHashMap();
	private static final Set<Block> TERRAIN_TO_SPREAD = Sets.newHashSet();
	private static final Set<Block> TERRAIN = Sets.newHashSet();
	
	public static void addSpreadableBlock(Block spreadableBlock, Block surfaceForSpread) {
		SPREADABLE_BLOCKS.put(spreadableBlock, surfaceForSpread);
		TERRAIN_TO_SPREAD.add(surfaceForSpread);
		TERRAIN.add(surfaceForSpread);
	}
	
	public static boolean isTerrain(Block block) {
		return TERRAIN.contains(block);
	}
	
	public static boolean isSpreadableTerrain(Block block) {
		return TERRAIN_TO_SPREAD.contains(block);
	}
	
	public static Block getSpreadable(Block block) {
		return SPREADABLE_BLOCKS.get(block);
	}
	
	public static void addLandGrass(Block plant, Block... terrain) {
		for (Block block : terrain) {
			addLandGrass(plant, block, 1F);
		}
	}
	
	public static void addLandGrass(ResourceLocation biome, Block plant, Block... terrain) {
		for (Block block : terrain) {
			addLandGrass(biome, plant, block, 1F);
		}
	}
	
	public static void addLandGrass(Block plant, Block terrain, float chance) {
		WeightedList<Block> list = LAND_GRASS_TYPES.get(terrain);
		if (list == null) {
			list = new WeightedList<Block>();
			LAND_GRASS_TYPES.put(terrain, list);
		}
		TERRAIN.add(terrain);
		list.add(plant, chance);
	}
	
	public static void addLandGrass(ResourceLocation biome, Block plant, Block terrain, float chance) {
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
		TERRAIN.add(terrain);
		list.add(plant, chance);
	}
	
	public static void addWaterGrass(Block plant, Block... terrain) {
		for (Block block : terrain) {
			addWaterGrass(plant, block, 1F);
		}
	}
	
	public static void addWaterGrass(ResourceLocation biome, Block plant, Block... terrain) {
		for (Block block : terrain) {
			addWaterGrass(biome, plant, block, 1F);
		}
	}
	
	public static void addWaterGrass(Block plant, Block terrain, float chance) {
		WeightedList<Block> list = WATER_GRASS_TYPES.get(terrain);
		if (list == null) {
			list = new WeightedList<Block>();
			WATER_GRASS_TYPES.put(terrain, list);
		}
		TERRAIN.add(terrain);
		list.add(plant, chance);
	}
	
	public static void addWaterGrass(ResourceLocation biome, Block plant, Block terrain, float chance) {
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
		TERRAIN.add(terrain);
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
		Map<Block, WeightedList<Block>> map = WATER_GRASS_BIOMES.get(biomeID);
		WeightedList<Block> list = null;
		if (map != null) {
			list = map.get(terrain);
			if (list == null) {
				list = WATER_GRASS_TYPES.get(terrain);
			}
		}
		else {
			list = WATER_GRASS_TYPES.get(terrain);
		}
		return list == null ? null : list.get(random);
	}
}
