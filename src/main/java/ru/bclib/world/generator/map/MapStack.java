package ru.bclib.world.generator.map;

import net.minecraft.util.Mth;
import org.apache.commons.lang3.function.TriFunction;
import ru.bclib.interfaces.BiomeMap;
import ru.bclib.noise.OpenSimplexNoise;
import ru.bclib.world.biomes.BCLBiome;
import ru.bclib.world.generator.BiomePicker;

import java.util.Random;

public class MapStack implements BiomeMap {
	private final OpenSimplexNoise noise;
	private final BiomeMap[] maps;
	private final int worldHeight;
	private final int minValue;
	private final int maxValue;
	
	public MapStack(long seed, int size, BiomePicker picker, int mapHeight, int worldHeight, TriFunction<Long, Integer, BiomePicker, BiomeMap> mapConstructor) {
		final int mapCount = Mth.ceil((float) worldHeight / mapHeight);
		this.worldHeight = worldHeight;
		minValue = Mth.floor(mapHeight * 0.5F + 0.5F);
		maxValue = Mth.floor(worldHeight - mapHeight * 0.5F + 0.5F);
		maps = new BiomeMap[mapCount];
		Random random = new Random(seed);
		for (int i = 0; i < mapCount; i++) {
			maps[i] = mapConstructor.apply(random.nextLong(), size, picker);
		}
		noise = new OpenSimplexNoise(random.nextInt());
	}
	
	@Override
	public void clearCache() {
		for (BiomeMap map: maps) {
			map.clearCache();
		}
	}
	
	@Override
	public BCLBiome getBiome(double x, double y, double z) {
		int mapIndex;
		
		if (y < minValue) {
			mapIndex = 0;
		}
		else if (y > maxValue) {
			mapIndex = maps.length - 1;
		}
		else {
			mapIndex = Mth.floor((y + noise.eval(x * 0.03, z * 0.03) * 8) / worldHeight * maps.length + 0.5F);
		}
		
		return maps[mapIndex].getBiome(x, y, z);
	}
}
